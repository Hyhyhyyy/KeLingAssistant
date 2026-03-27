import { Router } from 'express';
import { getNotes, createNote, updateNote, deleteNote } from '../controllers/notes';
import { authMiddleware } from '../middleware/auth';

const router = Router();

router.get('/', authMiddleware, getNotes);
router.post('/', authMiddleware, createNote);
router.put('/:id', authMiddleware, updateNote);
router.delete('/:id', authMiddleware, deleteNote);

export default router;