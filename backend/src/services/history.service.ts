import HistoryLog from '../models/HistoryLog';
import mongoose from 'mongoose';

export const logHistory = async (
  action: 'CREATE' | 'UPDATE' | 'DELETE',
  entityType: 'Component' | 'Customer' | 'Mold',
  entityId: mongoose.Types.ObjectId | string,
  userId: mongoose.Types.ObjectId | string,
  details: Record<string, any>
) => {
  try {
    const log = new HistoryLog({
      action,
      entityType,
      entityId,
      userId,
      details,
    });
    await log.save();
  } catch (error) {
    console.error('Error logging history:', error);
  }
};
