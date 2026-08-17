import { Response } from 'express';
import Component from '../models/Component';
import { logHistory } from '../services/history.service';
import { AuthRequest } from '../middleware/auth.middleware';

export const createComponent = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const component = new Component(req.body);
    await component.save();
    await logHistory('CREATE', 'Component', component.id, req.user.id, { new: component.toObject() });
    res.status(201).json(component);
  } catch (error) {
    res.status(500).json({ message: 'Error creating component' });
  }
};

export const getComponents = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const components = await Component.find().populate('customerId');
    res.json(components);
  } catch (error) {
    res.status(500).json({ message: 'Error fetching components' });
  }
};

export const getComponentById = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const component = await Component.findById(req.params.id).populate('customerId');
    if (!component) {
      res.status(404).json({ message: 'Component not found' });
      return;
    }
    res.json(component);
  } catch (error) {
    res.status(500).json({ message: 'Error fetching component' });
  }
};

import { sendNotification } from '../services/notification.service';

export const updateComponent = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const oldComponent = await Component.findById(req.params.id);
    if (!oldComponent) {
      res.status(404).json({ message: 'Component not found' });
      return;
    }
    const component = await Component.findByIdAndUpdate(req.params.id, req.body, { new: true });
    if (component) {
      await logHistory('UPDATE', 'Component', component.id, req.user.id, { old: oldComponent.toObject(), new: component.toObject() });
      
      if (component.stock < component.threshold && oldComponent.stock >= oldComponent.threshold) {
        await sendNotification('low_stock', 'Low Stock Alert', `Component ${component.name} is below threshold with stock: ${component.stock}`);
      }

      res.json(component);
    }
  } catch (error) {
    res.status(500).json({ message: 'Error updating component' });
  }
};

export const deleteComponent = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const component = await Component.findByIdAndDelete(req.params.id);
    if (!component) {
      res.status(404).json({ message: 'Component not found' });
      return;
    }
    await logHistory('DELETE', 'Component', component.id, req.user.id, { deleted: component.toObject() });
    res.json({ message: 'Component deleted' });
  } catch (error) {
    res.status(500).json({ message: 'Error deleting component' });
  }
};
