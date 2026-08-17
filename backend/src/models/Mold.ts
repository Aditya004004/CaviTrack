import mongoose, { Document, Schema } from 'mongoose';

export interface IMold extends Document {
  name: string;
  cavitations: number;
  customerId: mongoose.Types.ObjectId;
  componentId: mongoose.Types.ObjectId;
  status: string;
  createdAt: Date;
  updatedAt: Date;
}

const MoldSchema: Schema = new Schema(
  {
    name: { type: String, required: true },
    cavitations: { type: Number, required: true },
    customerId: { type: mongoose.Schema.Types.ObjectId, ref: 'Customer', required: true },
    componentId: { type: mongoose.Schema.Types.ObjectId, ref: 'Component', required: true },
    status: { type: String, enum: ['Active', 'In Maintenance', 'Retired'], default: 'Active' },
  },
  { timestamps: true }
);

export default mongoose.model<IMold>('Mold', MoldSchema);
