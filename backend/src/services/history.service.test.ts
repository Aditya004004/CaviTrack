import { logHistory } from './history.service';
import HistoryLog from '../models/HistoryLog';
import mongoose from 'mongoose';

jest.mock('../models/HistoryLog');

describe('History Service', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should create and save a history log successfully', async () => {
    const saveMock = jest.fn().mockResolvedValue(true);
    (HistoryLog as unknown as jest.Mock).mockImplementation(() => ({
      save: saveMock,
    }));

    const entityId = new mongoose.Types.ObjectId();
    const userId = new mongoose.Types.ObjectId();

    await logHistory('CREATE', 'Component', entityId, userId, { key: 'value' });

    expect(HistoryLog).toHaveBeenCalledWith({
      action: 'CREATE',
      entityType: 'Component',
      entityId,
      userId,
      details: { key: 'value' },
    });
    expect(saveMock).toHaveBeenCalled();
  });

  it('should log an error if saving fails', async () => {
    const saveMock = jest.fn().mockRejectedValue(new Error('Save failed'));
    (HistoryLog as unknown as jest.Mock).mockImplementation(() => ({
      save: saveMock,
    }));

    const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    const entityId = new mongoose.Types.ObjectId();
    const userId = new mongoose.Types.ObjectId();

    await logHistory('CREATE', 'Component', entityId, userId, { key: 'value' });

    expect(consoleErrorSpy).toHaveBeenCalledWith('Error logging history:', expect.any(Error));
    consoleErrorSpy.mockRestore();
  });
});
