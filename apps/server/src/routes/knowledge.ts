import { Router } from 'express';
import {
  getNodes,
  getNode,
  createNode,
  updateNode,
  deleteNode,
  batchCreateNodes
} from '../controllers/knowledge';
import { authMiddleware } from '../middleware/auth';

const router = Router();

router.get('/:courseId/nodes', authMiddleware, getNodes);
router.get('/node/:id', authMiddleware, getNode);
router.post('/:courseId/nodes', authMiddleware, createNode);
router.post('/:courseId/nodes/batch', authMiddleware, batchCreateNodes);
router.put('/node/:id', authMiddleware, updateNode);
router.delete('/node/:id', authMiddleware, deleteNode);

export default router;