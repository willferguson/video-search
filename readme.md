Basic outline of Video Metadata extraction and searching. 

Ingest flow works something like:

* Receive video as input stream
* Store video in storage
* Set status to STORED
* Create basic document in elastic
* Return to caller with video id. 

* On "new video" event from S3"
* Send for frame extraction (thread pool)
* On process start set status to EXTRACTING_FRAMES
* When finished set status to FRAMES_EXTRACTED

* Store timestamp in elastic document
* Store frames in S3

* Send event to image analysis for each uploaded frame (or for all?)
* Set status to analysing_frames
* Analyse frames with image analysis tools. 
* Index into elastic
* Set status to complete


Currently identified 3 services for later separation

* Frame extraction service - receives video, breaks into frames, indexes, stores
* Image Analysis service - pulls from storage, generates metadata, indexes
* Index service - indexes metadata, enables later searching.






