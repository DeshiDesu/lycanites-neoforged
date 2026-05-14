package com.lycanitesmobs.client.model.animation;

public interface IAnimationModel {

	/**
	 * Performs a rotation around an angle.
	 * @param rotation The amount to rotate by in degrees.
	 * @param angleX The x angle to rotate by, can range from -1 to 1.
	 * @param angleY The y angle to rotate by, can range from -1 to 1
	 * @param angleZ The z angle to rotate by, can range from -1 to 1
	 */
	void doAngle(float rotation, float angleX, float angleY, float angleZ);

	/**
	 * Performs a rotation.
	 * @param rotX The x amount to rotate by in degrees.
	 * @param rotY The y amount to rotate by in degrees.
	 * @param rotZ The z amount to rotate by in degrees.
	 */
	void doRotate(float rotX, float rotY, float rotZ);

	/**
	 * Performs a translation.
	 * @param posX The x amount to translate by.
	 * @param posY The y amount to translate by.
	 * @param posZ The z amount to translate by.
	 */
	void doTranslate(float posX, float posY, float posZ);

	/**
	 * Performs a scale.
	 * @param scaleX The x amount to scale by.
	 * @param scaleY The y amount to scale by.
	 * @param scaleZ The z amount to scale by.
	 */
	void doScale(float scaleX, float scaleY, float scaleZ);


	void angle(float rotation, float angleX, float angleY, float angleZ);

	void rotate(float rotX, float rotY, float rotZ);

	void translate(float posX, float posY, float posZ);

	void scale(float scaleX, float scaleY, float scaleZ);

	double rotateToPoint(double aTarget, double bTarget);
	double rotateToPoint(double aCenter, double bCenter, double aTarget, double bTarget);
	double[] rotateToPoint(double xCenter, double yCenter, double zCenter, double xTarget, double yTarget, double zTarget);
}
