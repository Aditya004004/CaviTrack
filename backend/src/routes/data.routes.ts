import { Router } from 'express';
import multer from 'multer';
import { exportCsv, importCsv } from '../controllers/data.controller';
import { authenticate } from '../middleware/auth.middleware';
import { authorizeRoles } from '../middleware/role.middleware';

const router = Router();
const upload = multer();

router.use(authenticate);

router.get('/export/csv', authorizeRoles('Admin', 'Staff'), exportCsv);
router.post('/import/csv', authorizeRoles('Admin', 'Staff'), upload.single('file'), importCsv);

export default router;
