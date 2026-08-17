import mongoose, { Document, Schema } from 'mongoose';

export interface IComponent extends Document {
  name: string;
  partNumber: string;
  description?: string;
  imageUrl?: string;
  customerId: mongoose.Types.ObjectId;
  stock: number;
  threshold: number;
  createdAt: Date;
  updatedAt: Date;
}

const ComponentSchema: Schema = new Schema(
  {
    name: { type: String, required: true },
    partNumber: { type: String, required: true, unique: true },
    description: { type: String },
    imageUrl: { type: String },
    customerId: { type: mongoose.Schema.Types.ObjectId, ref: 'Customer', required: true },
    stock: { type: Number, default: 0 },
    threshold: { type: Number, default: 10 },
  },
  { timestamps: true }
);

export default mongoose.model<IComponent>('Component', ComponentSchema);
