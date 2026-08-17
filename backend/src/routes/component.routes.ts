import { Router } from 'express';
import { createComponent, getComponents, getComponentById, updateComponent, deleteComponent } from '../controllers/component.controller';
import { authenticate } from '../middleware/auth.middleware';
import { authorizeRoles } from '../middleware/role.middleware';

const router = Router();

router.use(authenticate);

router.post('/', authorizeRoles('Admin', 'Staff'), createComponent);
router.get('/', authorizeRoles('Admin', 'Staff', 'Viewer'), getComponents);
router.get('/:id', authorizeRoles('Admin', 'Staff', 'Viewer'), getComponentById);
router.put('/:id', authorizeRoles('Admin', 'Staff'), updateComponent);
router.delete('/:id', authorizeRoles('Admin'), deleteComponent);

export default router;
