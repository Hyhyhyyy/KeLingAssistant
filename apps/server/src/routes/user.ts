import { Router } from 'express';
import { getProfile, updateProfile, updateEnergy, updateCrystals } from '../controllers/user';
import { authMiddleware } from '../middleware/auth';

const router = Router();

router.get('/profile', authMiddleware, getProfile);
router.put('/profile', authMiddleware, updateProfile);
router.post('/energy', authMiddleware, updateEnergy);
router.post('/crystals', authMiddleware, updateCrystals);

export default router;