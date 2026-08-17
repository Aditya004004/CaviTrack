import { Router } from 'express';
import multer from 'multer';
import { uploadImage } from '../controllers/upload.controller';
import { authenticate } from '../middleware/auth.middleware';

const router = Router();

// Configure multer for local temporary storage
const upload = multer({ dest: 'uploads/' });

router.use(authenticate);

router.post('/', upload.single('image'), uploadImage);

export default router;
