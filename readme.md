High Level Functionality

* Can ingest video, split into frames and submit each frames to Google / Microsoft etc for analysis. 

* Can view a list of all videos, with aggregations for tags. 
* Can select a specific tag aggregation, which searches for all frames with that tag and returns results & aggregations. 
* Can directly search a video metadata - name, uuid - this will return hits & facets for tag aggregations. 


* Search functionality
** get all aggregations with hits
** get aggrega






Basic outline of Video Metadata extraction and searching. 

Ingest flow works something like:

* Receive video as input stream
* Store video in storage
* Set status to STORED
* Create basic document in elastic
* Return to caller with video id. 

* On "new video" event from storage
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









