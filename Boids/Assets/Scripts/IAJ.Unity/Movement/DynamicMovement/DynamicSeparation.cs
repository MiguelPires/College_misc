using System;
using System.Collections.Generic;
using UnityEngine;

namespace Assets.Scripts.IAJ.Unity.Movement.DynamicMovement
{
	public class DynamicSeparation : DynamicMovement
	{
		public override string Name {
			get { return "Separation"; }
		}

		public float SeparationFactor  { get; set; }
		public List<DynamicCharacter> Flock { get; set; }
		public float FlockRadius { get; set; }
		public override KinematicData Target { get; set; }

		public DynamicSeparation ()
		{
		}

		public override MovementOutput GetMovement ()
		{
			var output = new MovementOutput ();

			foreach (DynamicCharacter boid in Flock) {

				if (Character == boid.KinematicData) {
					continue;
				}

				Vector3 direction = Character.position - boid.KinematicData.position;
				float distance = direction.magnitude;

				if (distance < FlockRadius) {
					float separationStrength = Math.Min (SeparationFactor / (distance * distance), MaxAcceleration);
					output.linear += direction.normalized * separationStrength;
				}
			}

			if (output.linear.magnitude > MaxAcceleration) {
				output.linear.Normalize ();
				output.linear *= MaxAcceleration;
			}
			output.angular = 0.0f;
			//Debug.DrawRay (Character.position, output.linear, Color.blue);
			return output;
		}
	}
}
