import { Router } from 'express';
import { chat, generatePlan, analyzeWeakness } from '../controllers/ai';
import { authMiddleware } from '../middleware/auth';

const router = Router();

router.post('/chat', authMiddleware, chat);
router.post('/plan', authMiddleware, generatePlan);
router.post('/analyze', authMiddleware, analyzeWeakness);

export default router;