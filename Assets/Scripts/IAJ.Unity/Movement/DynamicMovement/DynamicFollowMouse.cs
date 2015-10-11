using UnityEngine;

namespace Assets.Scripts.IAJ.Unity.Movement.DynamicMovement {
	public class DynamicFollowMouse : DynamicVelocityMatch {
		public override string Name {
			get { return "FollowMouse"; }
		}

		public float MaxSpeed { get; set; }
		public float StopRadius { get; set; }
		public float SlowRadius { get; set; }

		public DynamicFollowMouse () {
			this.Target = new KinematicData();
			this.MovingTarget = new KinematicData();
		}

		public override MovementOutput GetMovement () {
			Vector3 direction = this.Target.position - this.Character.position;

			float distance = direction.magnitude;

			float targetSpeed;

			if (distance < StopRadius)
			{
			    Arrived.Add(Character);
				return new MovementOutput();
			}

			if (distance > SlowRadius) {
				targetSpeed = MaxSpeed;
			} else {
				targetSpeed = MaxSpeed * (distance / SlowRadius);
			}

			MovingTarget.velocity = direction.normalized * targetSpeed;
			
			var output = base.GetMovement();
			//Debug.DrawRay(this.Character.position, output.linear, Color.blue);
			return output;
		}
	}
}
