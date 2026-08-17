import { Router } from 'express';
import { getAnalytics } from '../controllers/analytics.controller';
import { authenticate } from '../middleware/auth.middleware';
import { authorizeRoles } from '../middleware/role.middleware';

const router = Router();

router.use(authenticate);

router.get('/', authorizeRoles('Admin', 'Staff', 'Viewer'), getAnalytics);

export default router;
