import { Router } from 'express';
import {
  getCourses,
  getCourse,
  createCourse,
  updateCourse,
  deleteCourse,
  updateMastery
} from '../controllers/courses';
import { authMiddleware } from '../middleware/auth';

const router = Router();

router.get('/', authMiddleware, getCourses);
router.get('/:id', authMiddleware, getCourse);
router.post('/', authMiddleware, createCourse);
router.put('/:id', authMiddleware, updateCourse);
router.delete('/:id', authMiddleware, deleteCourse);
router.post('/:id/mastery', authMiddleware, updateMastery);

export default router;