import { Router } from 'express';
import {
  getTasks,
  getTask,
  createTask,
  updateTask,
  deleteTask,
  completeTask
} from '../controllers/tasks';
import { authMiddleware } from '../middleware/auth';

const router = Router();

router.get('/', authMiddleware, getTasks);
router.get('/:id', authMiddleware, getTask);
router.post('/', authMiddleware, createTask);
router.put('/:id', authMiddleware, updateTask);
router.delete('/:id', authMiddleware, deleteTask);
router.post('/:id/complete', authMiddleware, completeTask);

export default router;