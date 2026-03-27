import { Router } from 'express';
import { checkIn, getCheckInRecords, getCheckInStatus } from '../controllers/checkin';
import { authMiddleware } from '../middleware/auth';

const router = Router();

router.post('/', authMiddleware, checkIn);
router.get('/records', authMiddleware, getCheckInRecords);
router.get('/status', authMiddleware, getCheckInStatus);

export default router;