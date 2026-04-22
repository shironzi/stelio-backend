1. use presigned url on get/upload files
   - Generate presigned upload URL
   - Client uploads file directly to storage (R2/S3)
   - Client notifies backend upload is complete
   - Backend verifies file exists
   - Backend saves/updates metadata in DB
   - Backend returns file URL to client
