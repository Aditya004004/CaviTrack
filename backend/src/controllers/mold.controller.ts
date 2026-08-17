import { Response } from 'express';
import Mold from '../models/Mold';
import { logHistory } from '../services/history.service';
import { AuthRequest } from '../middleware/auth.middleware';

export const createMold = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const mold = new Mold(req.body);
    await mold.save();
    await logHistory('CREATE', 'Mold', mold.id, req.user.id, { new: mold.toObject() });
    res.status(201).json(mold);
  } catch (error) {
    res.status(500).json({ message: 'Error creating mold' });
  }
};

export const getMolds = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const molds = await Mold.find().populate('customerId').populate('componentId');
    res.json(molds);
  } catch (error) {
    res.status(500).json({ message: 'Error fetching molds' });
  }
};

export const getMoldById = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const mold = await Mold.findById(req.params.id).populate('customerId').populate('componentId');
    if (!mold) {
      res.status(404).json({ message: 'Mold not found' });
      return;
    }
    res.json(mold);
  } catch (error) {
    res.status(500).json({ message: 'Error fetching mold' });
  }
};

import { sendNotification } from '../services/notification.service';

export const updateMold = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const oldMold = await Mold.findById(req.params.id);
    if (!oldMold) {
      res.status(404).json({ message: 'Mold not found' });
      return;
    }
    const mold = await Mold.findByIdAndUpdate(req.params.id, req.body, { new: true });
    if (mold) {
      await logHistory('UPDATE', 'Mold', mold.id, req.user.id, { old: oldMold.toObject(), new: mold.toObject() });

      if (oldMold.status !== 'In Maintenance' && mold.status === 'In Maintenance') {
        await sendNotification('maintenance', 'Mold Maintenance Alert', `Mold ${mold.name} status changed to In Maintenance`);
      }

      res.json(mold);
    }
  } catch (error) {
    res.status(500).json({ message: 'Error updating mold' });
  }
};

export const deleteMold = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const mold = await Mold.findByIdAndDelete(req.params.id);
    if (!mold) {
      res.status(404).json({ message: 'Mold not found' });
      return;
    }
    await logHistory('DELETE', 'Mold', mold.id, req.user.id, { deleted: mold.toObject() });
    res.json({ message: 'Mold deleted' });
  } catch (error) {
    res.status(500).json({ message: 'Error deleting mold' });
  }
};
