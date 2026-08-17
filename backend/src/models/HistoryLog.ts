import mongoose, { Document, Schema } from 'mongoose';

export interface IHistoryLog extends Document {
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  entityType: 'Component' | 'Customer' | 'Mold';
  entityId: mongoose.Types.ObjectId;
  userId: mongoose.Types.ObjectId;
  details: Record<string, any>;
  createdAt: Date;
  updatedAt: Date;
}

const HistoryLogSchema: Schema = new Schema(
  {
    action: { type: String, enum: ['CREATE', 'UPDATE', 'DELETE'], required: true },
    entityType: { type: String, enum: ['Component', 'Customer', 'Mold'], required: true },
    entityId: { type: mongoose.Schema.Types.ObjectId, required: true },
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    details: { type: mongoose.Schema.Types.Mixed, required: true },
  },
  { timestamps: true }
);

export default mongoose.model<IHistoryLog>('HistoryLog', HistoryLogSchema);
