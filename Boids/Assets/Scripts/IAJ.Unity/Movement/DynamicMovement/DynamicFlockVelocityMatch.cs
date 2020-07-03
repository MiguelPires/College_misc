using Assets.Scripts.IAJ.Unity.Util;
using System;
using System.Collections.Generic;
using UnityEngine;

namespace Assets.Scripts.IAJ.Unity.Movement.DynamicMovement
{
    public class DynamicFlockVelocityMatch : DynamicVelocityMatch
    {
        public override string Name
        {
            get { return "FlockVelocityMatch"; }
        }

        public List<DynamicCharacter> Flock { get; set; }
        public float FlockRadius { get; set; }
        public float FanAngle { get; set; }

        public DynamicFlockVelocityMatch()
        {
            MovingTarget = new KinematicData();
        }

        public override MovementOutput GetMovement()
        {
            Vector3 averageVelocity = new Vector3();
            int closeBoids = 0;

            foreach (DynamicCharacter boid in Flock)
            {
                if (Character == boid.KinematicData)
                {
                    continue;
                }

                Vector3 direction = boid.KinematicData.position - Character.position;

                if (direction.magnitude <= FlockRadius)
                {
                    float angle = MathHelper.ConvertVectorToOrientation(direction);
                    float angleDiff = MathHelper.ShortestAngleDifference(this.Character.orientation, angle);

                    if (Math.Abs(angleDiff) <= FanAngle)
                    {
                        averageVelocity += boid.KinematicData.velocity;
                        closeBoids++;
                    }
                }
            }

            if (closeBoids == 0)
            {
                return new MovementOutput();
            }

            averageVelocity /= closeBoids;
            MovingTarget.velocity = averageVelocity;

            var output = base.GetMovement();
            //Debug.DrawRay(this.Character.position, MovingTarget.velocity, Color.black);
            return output;
        }
    }
}