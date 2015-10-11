using Assets.Scripts.IAJ.Unity.Util;
using System.Collections;
using UnityEngine;

namespace Assets.Scripts.IAJ.Unity.Movement.DynamicMovement
{
    public class DynamicAvoidObstacle : DynamicSeek
    {
        public Collider collisionDetector;
        public float AvoidMargin;
        public float MaxLookAhead;
        private float epsilon = 0.00001F;

        public override string Name
        {
            get { return "DynamicAvoidObstacle"; }
        }

        public DynamicAvoidObstacle(GameObject obstacle)
        {
            this.collisionDetector = obstacle.GetComponent<Collider>();
            this.Target = new KinematicData();
        }

        public override MovementOutput GetMovement()
        {
            if (this.Character.velocity.magnitude < epsilon)
                return new MovementOutput();

            // central ray
            Debug.DrawRay(this.Character.position, this.Character.velocity.normalized * MaxLookAhead/2.0f, this.MovementDebugColor);

            bool willCollide = false;

            MovementOutput output = checkCollision(this.Character.velocity, this.MaxLookAhead/2.0f, out willCollide);

            if (willCollide)
                return output;

            // right wisker at a 30 degree angle
            Vector3 rightWhisker = MathHelper.ConvertOrientationToVector(MathHelper.ConvertVectorToOrientation(this.Character.velocity) + MathConstants.MATH_PI_2 / 3.0F);
            Debug.DrawRay(this.Character.position, rightWhisker.normalized * MaxLookAhead/3.0f, this.MovementDebugColor);

            output = checkCollision(rightWhisker, this.MaxLookAhead / 3.0f, out willCollide);

            if (willCollide)
                return output;

            // left wisker at a 30 degree angle
            Vector3 leftWhisker = MathHelper.ConvertOrientationToVector(MathHelper.ConvertVectorToOrientation(this.Character.velocity) - MathConstants.MATH_PI_2 / 3.0F);
            Debug.DrawRay(this.Character.position, leftWhisker.normalized *MaxLookAhead / 3.0f, this.MovementDebugColor);
            output = checkCollision(leftWhisker, this.MaxLookAhead / 3.0f, out willCollide);

            if (willCollide)
                return output;

            return new MovementOutput();
        }

        private MovementOutput checkCollision(Vector3 vec, float MaxLookAhead, out bool willCollide)
        {
            Ray ray = new Ray(this.Character.position, vec);
            RaycastHit collision = new RaycastHit();

            if (collisionDetector.Raycast(ray, out collision, MaxLookAhead))
            {
                willCollide = true;
                this.Target.position = collision.point + collision.normal * AvoidMargin;
                return base.GetMovement();
            }
            else
            {
                willCollide = false;
            }
            return null;
        }
    }
}