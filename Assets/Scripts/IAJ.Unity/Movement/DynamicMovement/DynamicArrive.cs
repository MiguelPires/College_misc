using UnityEngine;
using System.Collections;
using Assets.Scripts.IAJ.Unity.Movement.DynamicMovement;

namespace Assets.Scripts.IAJ.Unity.Movement.DynamicMovement {
	public class DynamicArrive : DynamicVelocityMatch {

		public override string Name {
			get { return "Arrive"; }
		}

		public float MaxSpeed { get; set; }
		public float StopRadius { get; set; }
		public float SlowRadius { get; set; }

		public DynamicArrive() {
			this.Target = new KinematicData();
			this.MovingTarget = new KinematicData();
		}

		// Update is called once per frame
		public override MovementOutput GetMovement () {
			Vector3 direction = this.Target.position - this.Character.position;
			float distance = direction.magnitude;
			float targetSpeed;

			if (distance < StopRadius)
				return new MovementOutput();

			if (distance > SlowRadius)
				targetSpeed = MaxSpeed;
			else
				targetSpeed = MaxSpeed * (distance / SlowRadius);

			this.MovingTarget = new KinematicData();
			this.MovingTarget.velocity = direction.normalized * targetSpeed;

			return base.GetMovement();
		}
	}

}
