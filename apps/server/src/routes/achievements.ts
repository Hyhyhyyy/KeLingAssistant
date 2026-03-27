import { Router } from 'express';
import { getAchievements, getUserAchievements } from '../controllers/achievements';
import { authMiddleware } from '../middleware/auth';

const router = Router();

router.get('/', authMiddleware, getAchievements);
router.get('/user', authMiddleware, getUserAchievements);

export default router;