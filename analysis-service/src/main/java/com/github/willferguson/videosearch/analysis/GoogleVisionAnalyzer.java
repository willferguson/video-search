package com.github.willferguson.videosearch.analysis;

import com.github.willferguson.videosearch.analysis.model.FrameAttribute;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by will on 02/10/2016.
 */
@Component
public class GoogleVisionAnalyzer implements ImageAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(GoogleVisionAnalyzer.class);
    private String bucketName;
    private Storage storage;
    private Vision vision;

    public GoogleVisionAnalyzer(String bucketName,
                                String applicationName,
                                ResourceLoader resourceLoader) {
        try {
            this.bucketName = bucketName;
            Resource resource = resourceLoader.getResource("classpath:google-credentials.json");
            List<String> scopes = new ArrayList<>();
            scopes.addAll(StorageScopes.all());
            scopes.addAll(VisionScopes.all());
            GoogleCredential googleCredential = GoogleCredential.fromStream(resource.getInputStream())
                    .createScoped(scopes);

            this.storage = new Storage.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                    googleCredential)
                    .setApplicationName(applicationName)
                    .build();

            this.vision = new Vision.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                    googleCredential)
                    .setApplicationName(applicationName)
                    .build();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> getAnalysisTypes() {
        return EnumSet.allOf(Type.class)
                .stream()
                .map(Enum::toString)
                .collect(Collectors.toSet());
    }

    @Override
    public Single<Map<String, Set<FrameAttribute>>> generateMetadata(InputStream inputStream, String contentType, long contentLength, Set<String> analysisTypes) {

        //Random file name so we never clash. We delete immediately after anyway
        String tempFilename = UUID.randomUUID().toString();
        return Single.fromCallable(
                () -> uploadContent(tempFilename, contentType, inputStream))
                .map(storageObject -> {
                    String objectName = "gs://"+this.bucketName+"/"+tempFilename;
                    ImageSource imageSource = getImageSource(objectName);
                    AnnotateImageRequest request = new AnnotateImageRequest().setImage(new Image().setSource(imageSource));
                    request.setFeatures(toFeatureList(analysisTypes, 5));
                    return request;
                })
                .map(request -> {
                    try {
                        Vision.Images.Annotate annotate = vision
                                .images()
                                .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));

                        BatchAnnotateImagesResponse batchResponse = annotate.execute();
                        return batchResponse.getResponses().get(0);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(response -> buildResponseMap(response, analysisTypes))
                .doAfterTerminate(() -> {
                    try {
                        storage.objects().delete(this.bucketName, tempFilename).execute();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        }

        private Map<String, Set<FrameAttribute>> buildResponseMap(AnnotateImageResponse response, Set<String> analysisTypes) {
            return analysisTypes
                    .stream()
                    .map(Type::valueOf)
                    .map(type -> type.extract(response))
                    //Want to remove when we have no values for the key.
                    .filter(entry -> !entry.getValue().isEmpty())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

    private List<Feature> toFeatureList(Set<String> types, int maxResult) {
        return types
                .stream()
                .map(type -> new Feature().setType(type).setMaxResults(maxResult))
                .collect(Collectors.toList());
    }

    private StorageObject uploadContent(String fileName, String contentType, InputStream inputStream) {
        try {
            StorageObject objectMetadata = new StorageObject().setName(fileName);
            InputStreamContent contentStream = new InputStreamContent(contentType, inputStream);
            Storage.Objects.Insert insertRequest = storage.objects().insert(this.bucketName, objectMetadata, contentStream);
            return insertRequest.execute();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ImageSource getImageSource(String fileName){
        return new ImageSource().setGcsImageUri(fileName);
    }

    private enum Type {


        LANDMARK_DETECTION(
                response -> {
                    return constructEntry("LANDMARK_DETECTION", response.getLandmarkAnnotations());
                }
        ),
        //TODO - Add support for face detection in Frame Data
        FACE_DETECTION(
                response -> {
                    return null;
                }
        ),
        LOGO_DETECTION(
                response -> {
                    return constructEntry("LOGO_DETECTION", response.getLogoAnnotations());
                }
        ),
        LABEL_DETECTION(
                response -> {
                    return constructEntry("LABEL_DETECTION", response.getLabelAnnotations());
                }
        ),
        TEXT_DETECTION(
                response -> {
                    return constructEntry("TEXT_DETECTION", response.getTextAnnotations());
                }
        ),
        //TODO - Add support for safe search detection in Frame Data
        SAFE_SEARCH_DETECTION(
                response -> {
                    return null;
                }
        ),
        //TODO - Add support for Image Properties in frame data.
        IMAGE_PROPERTIES(
                response -> {
                    return null;
                }
        );


        private Function<AnnotateImageResponse, Map.Entry<String, Set<FrameAttribute>>> extractionFunction;

        Type(Function<AnnotateImageResponse, Map.Entry<String, Set<FrameAttribute>>> extractionFunction) {
            this.extractionFunction = extractionFunction;
        }

        public Map.Entry<String, Set<FrameAttribute>> extract(AnnotateImageResponse response) {
            return extractionFunction.apply(response);
        }

        private static Map.Entry<String, Set<FrameAttribute>> constructEntry(String annotationType, List<EntityAnnotation> annotations) {
            //Annoyingly google returns null as opposed to empty list if the analysis wasn't successful. (EG no logo)
            if (annotations == null) {
                annotations = Collections.emptyList();
            }
            Set<FrameAttribute> frameAttributes = annotations.stream()
                .map(entityAnnotation -> {
                    String name = entityAnnotation.getDescription();
                    double confidence = entityAnnotation.getScore();
                    return new FrameAttribute(name, confidence);
                })
                .collect(Collectors.toSet());
            return new AbstractMap.SimpleEntry<>(annotationType, frameAttributes);
        }
    }

}
