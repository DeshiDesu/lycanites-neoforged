package com.lycanitesmobs.client.model.animation;

public class Animator {
	protected IAnimationModel model;

	public Animator(IAnimationModel model) {
		this.model = model;
	}

	public void doAngle(float rotation, float angleX, float angleY, float angleZ) {
		this.model.doAngle(rotation, angleX, angleY, angleZ);
	}

	public void doRotate(float rotX, float rotY, float rotZ) {
		this.model.doRotate(rotX, rotY, rotZ);
	}

	public void doTranslate(float posX, float posY, float posZ) {
		this.model.doTranslate(posX, posY, posZ);
	}

	public void doScale(float scaleX, float scaleY, float scaleZ) {
		this.model.doScale(scaleX, scaleY, scaleZ);
	}
}
