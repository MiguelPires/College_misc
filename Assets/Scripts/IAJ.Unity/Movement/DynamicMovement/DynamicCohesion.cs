using Assets.Scripts.IAJ.Unity.Util;
using System;
using System.Collections.Generic;
using UnityEngine;

namespace Assets.Scripts.IAJ.Unity.Movement.DynamicMovement
{
    public class DynamicCohesion : DynamicArrive
    {
        public override string Name
        {
            get { return "Cohesion"; }
        }

        public List<DynamicCharacter> Flock { get; set; }
        public float FlockRadius { get; set; }
        public override KinematicData Target { get; set; }
        public float FanAngle { get; set; }

        public DynamicCohesion()
        {
            Target = new KinematicData();
        }

        public override MovementOutput GetMovement()
        {
            Vector3 massCenter = new Vector3();
            int closeBoids = 0;

            foreach (DynamicCharacter boid in Flock)
            {
                if (Character == boid.KinematicData)
                {
                    continue;
                }

                Vector3 direction = boid.KinematicData.position - Character.position;
                float distance = direction.magnitude;

                if (distance < FlockRadius)
                {
                    float angle = MathHelper.ConvertVectorToOrientation(direction);
                    float angleDiff = MathHelper.ShortestAngleDifference(this.Character.orientation, angle);

                    if (Math.Abs(angleDiff) <= FanAngle)
                    {
                        massCenter += boid.KinematicData.position;
                        ++closeBoids;
                    }
                }
            }

            if (closeBoids == 0)
                return new MovementOutput();

            Target.position = massCenter / ((float)closeBoids);
            var output = base.GetMovement();
            return output;
        }
    }
}