import { Response } from 'express';
import Customer from '../models/Customer';
import { logHistory } from '../services/history.service';
import { AuthRequest } from '../middleware/auth.middleware';

export const createCustomer = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const customer = new Customer(req.body);
    await customer.save();
    await logHistory('CREATE', 'Customer', customer.id, req.user.id, { new: customer.toObject() });
    res.status(201).json(customer);
  } catch (error) {
    res.status(500).json({ message: 'Error creating customer' });
  }
};

export const getCustomers = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const customers = await Customer.find();
    res.json(customers);
  } catch (error) {
    res.status(500).json({ message: 'Error fetching customers' });
  }
};

export const getCustomerById = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const customer = await Customer.findById(req.params.id);
    if (!customer) {
      res.status(404).json({ message: 'Customer not found' });
      return;
    }
    res.json(customer);
  } catch (error) {
    res.status(500).json({ message: 'Error fetching customer' });
  }
};

export const updateCustomer = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const oldCustomer = await Customer.findById(req.params.id);
    if (!oldCustomer) {
      res.status(404).json({ message: 'Customer not found' });
      return;
    }
    const customer = await Customer.findByIdAndUpdate(req.params.id, req.body, { new: true });
    if (customer) {
      await logHistory('UPDATE', 'Customer', customer.id, req.user.id, { old: oldCustomer.toObject(), new: customer.toObject() });
      res.json(customer);
    }
  } catch (error) {
    res.status(500).json({ message: 'Error updating customer' });
  }
};

export const deleteCustomer = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const customer = await Customer.findByIdAndDelete(req.params.id);
    if (!customer) {
      res.status(404).json({ message: 'Customer not found' });
      return;
    }
    await logHistory('DELETE', 'Customer', customer.id, req.user.id, { deleted: customer.toObject() });
    res.json({ message: 'Customer deleted' });
  } catch (error) {
    res.status(500).json({ message: 'Error deleting customer' });
  }
};
