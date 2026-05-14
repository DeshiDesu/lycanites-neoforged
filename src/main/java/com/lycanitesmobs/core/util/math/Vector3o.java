package com.lycanitesmobs.core.util.math;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;


public class Vector3o {

    public static Vector3o XN = new Vector3o(-1.0F, 0.0F, 0.0F);
    public static Vector3o XP = new Vector3o(1.0F, 0.0F, 0.0F);
    public static Vector3o YN = new Vector3o(0.0F, -1.0F, 0.0F);
    public static Vector3o YP = new Vector3o(0.0F, 1.0F, 0.0F);
    public static Vector3o ZN = new Vector3o(0.0F, 0.0F, -1.0F);
    public static Vector3o ZP = new Vector3o(0.0F, 0.0F, 1.0F);
    public float x;
    public float y;
    public float z;

    public Vector3o() {
    }

    public Vector3o(float p_i48098_1_, float p_i48098_2_, float p_i48098_3_) {
        this.x = p_i48098_1_;
        this.y = p_i48098_2_;
        this.z = p_i48098_3_;
    }

    // Forge start
    public Vector3o(float[] values) {
        set(values);
    }

    public Vector3o(Vector3o copy) {
        this.x = copy.x;
        this.y = copy.y;
        this.z = copy.z;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            Vector3o vector3f = (Vector3o) p_equals_1_;
            if (Float.compare(vector3f.x, this.x) != 0) {
                return false;
            } else if (Float.compare(vector3f.y, this.y) != 0) {
                return false;
            } else {
                return Float.compare(vector3f.z, this.z) == 0;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int i = Float.floatToIntBits(this.x);
        i = 31 * i + Float.floatToIntBits(this.y);
        return 31 * i + Float.floatToIntBits(this.z);
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    public void mul(float p_195898_1_) {
        this.x *= p_195898_1_;
        this.y *= p_195898_1_;
        this.z *= p_195898_1_;
    }

    public void mul(float p_229192_1_, float p_229192_2_, float p_229192_3_) {
        this.x *= p_229192_1_;
        this.y *= p_229192_2_;
        this.z *= p_229192_3_;
    }

    public void clamp(float p_195901_1_, float p_195901_2_) {
        this.x = Mth.clamp(this.x, p_195901_1_, p_195901_2_);
        this.y = Mth.clamp(this.y, p_195901_1_, p_195901_2_);
        this.z = Mth.clamp(this.z, p_195901_1_, p_195901_2_);
    }

    public void set(float p_195905_1_, float p_195905_2_, float p_195905_3_) {
        this.x = p_195905_1_;
        this.y = p_195905_2_;
        this.z = p_195905_3_;
    }

    public void add(float p_195904_1_, float p_195904_2_, float p_195904_3_) {
        this.x += p_195904_1_;
        this.y += p_195904_2_;
        this.z += p_195904_3_;
    }

    public void add(Vector3o p_229189_1_) {
        this.x += p_229189_1_.x;
        this.y += p_229189_1_.y;
        this.z += p_229189_1_.z;
    }

    public Vector3o sub(Vector3o p_195897_1_) {
        this.x -= p_195897_1_.x;
        this.y -= p_195897_1_.y;
        this.z -= p_195897_1_.z;
        return this;
    }

    public float dot(Vector3o p_195903_1_) {
        return this.x * p_195903_1_.x + this.y * p_195903_1_.y + this.z * p_195903_1_.z;
    }

    public boolean normalize() {
        float f = this.x * this.x + this.y * this.y + this.z * this.z;
        if ((double) f < 1.0E-5D) {
            return false;
        } else {
            float f1 = LMHelperClass.convertToFloat(Mth.fastInvSqrt(f));
            this.x *= f1;
            this.y *= f1;
            this.z *= f1;
            return true;
        }
    }

    public void cross(Vector3o p_195896_1_) {
        float f = this.x;
        float f1 = this.y;
        float f2 = this.z;
        float f3 = p_195896_1_.x();
        float f4 = p_195896_1_.y();
        float f5 = p_195896_1_.z();
        this.x = f1 * f5 - f2 * f4;
        this.y = f2 * f3 - f * f5;
        this.z = f * f4 - f1 * f3;
    }

    public void lerp(Vector3o p_229190_1_, float p_229190_2_) {
        float f = 1.0F - p_229190_2_;
        this.x = this.x * f + p_229190_1_.x * p_229190_2_;
        this.y = this.y * f + p_229190_1_.y * p_229190_2_;
        this.z = this.z * f + p_229190_1_.z * p_229190_2_;
    }

    public Vector3o copy() {
        return new Vector3o(this.x, this.y, this.z);
    }

    public void map(Float2FloatFunction p_229191_1_) {
        this.x = p_229191_1_.get(this.x);
        this.y = p_229191_1_.get(this.y);
        this.z = p_229191_1_.get(this.z);
    }

    public Vec3 toVec3() {
        return new Vec3(this.x, this.y, this.z);
    }

    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }

    public void set(float[] values) {
        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public Quaternionf rotationDegrees(float p_229187_1_) {
        return rotationDegrees(p_229187_1_, true);
    }

    public Quaternionf rotationDegrees(float p_i48101_2_, boolean p_i48101_3_) {
        if (p_i48101_3_) {
            p_i48101_2_ *= ((float) Math.PI / 180F);
        }

        float f = sin(p_i48101_2_ / 2.0F);
        float i = this.x() * f;
        float j = this.y() * f;
        float k = this.z() * f;
        float r = cos(p_i48101_2_ / 2.0F);
        return new Quaternionf(i, j, k, r);
    }

    private float sin(float p_214903_0_) {
        return (float) Math.sin((double) p_214903_0_);
    }

    private float cos(float p_214904_0_) {
        return (float) Math.cos((double) p_214904_0_);
    }

}
