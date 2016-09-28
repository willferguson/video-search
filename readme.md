Basic outline of Video Metadata extraction and searching. 

Ingest flow works something like:

Consume video
Break into frames with timestamp file
Index into search system basic info for frames. 
Push frames into storage (eg s3)

Analyse frames with image analysis tools. 
Index metadata

Currently identified 3 services for later separation

Frame extraction service - receives video, breaks into frames, indexes, stores
Image Analysis service - pulls from storage, generates metadata, indexes
Index service - indexes metadata, enables later searching.






