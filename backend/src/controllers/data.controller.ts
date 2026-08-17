import { Response } from 'express';
import { AuthRequest } from '../middleware/auth.middleware';
import Component from '../models/Component';
import HistoryLog from '../models/HistoryLog';
import { Parser } from 'json2csv';
import csvParser from 'csv-parser';
import { Readable } from 'stream';

export const exportCsv = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    const type = req.query.type as string;
    let data;
    if (type === 'history') {
      data = await HistoryLog.find().lean();
    } else {
      data = await Component.find().lean();
    }
    
    if (!data || data.length === 0) {
      res.status(404).json({ message: 'No data found to export' });
      return;
    }

    const parser = new Parser();
    const csv = parser.parse(data);

    res.header('Content-Type', 'text/csv');
    res.attachment(`${type || 'components'}.csv`);
    res.send(csv);
  } catch (error) {
    res.status(500).json({ message: 'Error exporting CSV' });
  }
};

export const importCsv = async (req: AuthRequest, res: Response): Promise<void> => {
  try {
    if (!req.file) {
      res.status(400).json({ message: 'No file uploaded' });
      return;
    }

    const results: any[] = [];
    const stream = Readable.from(req.file.buffer);
    
    stream
      .pipe(csvParser())
      .on('data', (data) => results.push(data))
      .on('end', async () => {
        for (const item of results) {
          try {
             const comp = new Component(item);
             await comp.save();
          } catch (e) {
             console.error('Error saving imported component', item, e);
          }
        }
        res.json({ message: 'Import successful', count: results.length });
      });
  } catch (error) {
    res.status(500).json({ message: 'Error importing CSV' });
  }
};
