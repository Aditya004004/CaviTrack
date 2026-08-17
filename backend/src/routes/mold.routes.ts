import { Router } from 'express';
import { createMold, getMolds, getMoldById, updateMold, deleteMold } from '../controllers/mold.controller';
import { authenticate } from '../middleware/auth.middleware';

const router = Router();

router.use(authenticate);

router.post('/', createMold);
router.get('/', getMolds);
router.get('/:id', getMoldById);
router.put('/:id', updateMold);
router.delete('/:id', deleteMold);

export default router;
