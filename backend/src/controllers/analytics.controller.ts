import { Response } from 'express';
import { AuthRequest } from '../middleware/auth.middleware';
import Component from '../models/Component';
import Mold from '../models/Mold';

export const getAnalytics = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const totalComponents = await Component.countDocuments();
    const totalMolds = await Mold.countDocuments();
    const activeMolds = await Mold.countDocuments({ status: 'Active' });
    const inMaintenanceMolds = await Mold.countDocuments({ status: 'In Maintenance' });
    
    const utilizationPercentage = totalMolds > 0 ? (activeMolds / totalMolds) * 100 : 0;

    res.json({
      totalComponents,
      totalMolds,
      activeMolds,
      inMaintenanceMolds,
      utilizationPercentage,
    });
  } catch (error) {
    res.status(500).json({ message: 'Error fetching analytics' });
  }
};
