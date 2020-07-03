using Assets.Scripts.IAJ.Unity.Movement;
using Assets.Scripts.IAJ.Unity.Movement.Arbitration;
using Assets.Scripts.IAJ.Unity.Movement.DynamicMovement;
using Assets.Scripts.IAJ.Unity.Util;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using UnityEngine.UI;
using Random = UnityEngine.Random;

public class PriorityManager : MonoBehaviour
{
    public const float X_WORLD_SIZE = 55;
    public const float Z_WORLD_SIZE = 32.5f;
    public const float AVOID_MARGIN = 8.0f;
    public const float COHESION_RADIUS = 20.0f;
    public const float SEPARATION_RADIUS = 20.0F;
    public const float MAX_SPEED = 20.0f;
    public const float MAX_ACCELERATION = 40.0f;
    public const float MAX_LOOK_AHEAD = 10.0f;
    public const float DRAG = 0.1f;

    private const int NUMBER_OF_BOIDS = 50;

    public const float SEPARATION_FACTOR = 40.0f;
    public const float STOP_RADIUS = 4.0F;
    public const float SLOW_RADIUS = 0.0F;
    public const float FAN_ANGLE = MathConstants.MATH_PI - MathConstants.MATH_PI_4;

    public const float MOUSE_CLICK_WEIGHT = 10.0F;
    public const float SEPARATION_WEIGHT = 3.0f;
    public const float COHESION_WEIGHT = 0.5F;
    public const float VELOCITY_MATCH_WEIGHT = 8.0F;

    public const int FLOCK_ARRIVE_FACTOR = 3;
    public const int FLOCK_ARRIVE_COUNT = (int)((NUMBER_OF_BOIDS + FLOCK_ARRIVE_FACTOR - 1) / FLOCK_ARRIVE_FACTOR);

    private MovementWithWeight FollowMouseMovement { get; set; }
    private bool IsFollowingMouse { get { return FollowMouseMovement.Weight > 0; } }

    private DynamicCharacter RedCharacter { get; set; }

    private Text RedMovementText { get; set; }

    private List<DynamicCharacter> Flock { get; set; }

    // Use this for initialization
    private void Start()
    {
        var redObj = GameObject.Find("Red");

        this.RedCharacter = new DynamicCharacter(redObj)
        {
            Drag = DRAG,
            MaxSpeed = MAX_SPEED
        };

        var obstacles = GameObject.FindGameObjectsWithTag("Obstacle");

        this.Flock = this.CloneSecondaryCharacters(redObj, NUMBER_OF_BOIDS - 1, obstacles);
        this.Flock.Add(this.RedCharacter);

        var allBoidMovements = InitBoidMovements(Flock, obstacles);

        foreach (var boid in Flock)
        {
            boid.Movement = new PriorityMovement(boid.KinematicData, allBoidMovements);
        }
    }

    private List<DynamicMovement> InitBoidMovements(List<DynamicCharacter> characters, IEnumerable<GameObject> obstacles)
    {
        List<DynamicMovement> avoidObstacleMovement = new List<DynamicMovement>();

        foreach (var obstacle in obstacles)
        {
            avoidObstacleMovement.Add(new DynamicAvoidObstacle(obstacle)
            {
                MaxAcceleration = MAX_ACCELERATION,
                AvoidMargin = AVOID_MARGIN,
                MaxLookAhead = MAX_LOOK_AHEAD,
                MovementDebugColor = Color.magenta
            });
        }

        var cohesion = new DynamicCohesion()
        {
            Flock = this.Flock,
            MaxSpeed = MAX_SPEED,
            MaxAcceleration = MAX_ACCELERATION,
            StopRadius = 0.0F,
            SlowRadius = 6.0F,
            FlockRadius = COHESION_RADIUS,
            FanAngle = FAN_ANGLE,
            MovementDebugColor = Color.red
        };

        var separation = new DynamicSeparation()
        {
            Flock = this.Flock,
            FlockRadius = SEPARATION_RADIUS,
            MaxAcceleration = MAX_ACCELERATION,
            SeparationFactor = SEPARATION_FACTOR,
            MovementDebugColor = Color.blue
        };

        var matchVelocity = new DynamicFlockVelocityMatch()
        {
            Flock = this.Flock,
            MaxAcceleration = MAX_ACCELERATION,
            FlockRadius = COHESION_RADIUS,
            FanAngle = FAN_ANGLE,
            MovementDebugColor = Color.green
        };

        var followMouse = new DynamicFollowMouse()
        {
            MaxSpeed = MAX_SPEED,
            MaxAcceleration = MAX_ACCELERATION,
            StopRadius = STOP_RADIUS,
            SlowRadius = SLOW_RADIUS,
            Arrived = new HashSet<KinematicData>(),
            MovementDebugColor = Color.gray
        };

        FollowMouseMovement = new MovementWithWeight(followMouse, 0);

        var blended = new BlendedMovement();
        blended.Movements.Add(new MovementWithWeight(cohesion, COHESION_WEIGHT));
        blended.Movements.Add(new MovementWithWeight(separation, SEPARATION_WEIGHT));
        blended.Movements.Add(new MovementWithWeight(matchVelocity, VELOCITY_MATCH_WEIGHT));
        blended.Movements.Add(FollowMouseMovement);

        var allMovements = new List<DynamicMovement>();
        allMovements.AddRange(avoidObstacleMovement);
        allMovements.Add(blended);

        return allMovements;
    }

    private List<DynamicCharacter> CloneSecondaryCharacters(GameObject objectToClone, int numberOfCharacters, GameObject[] obstacles)
    {
        var characters = new List<DynamicCharacter>();
        for (int i = 0; i < numberOfCharacters; i++)
        {
            var clone = GameObject.Instantiate(objectToClone);

            clone.transform.position = this.GenerateRandomClearPosition(obstacles);
            var character = new DynamicCharacter(clone)
            {
                MaxSpeed = MAX_SPEED,
                Drag = DRAG
            };
            characters.Add(character);
        }

        return characters;
    }

    private Vector3 GenerateRandomClearPosition(GameObject[] obstacles)
    {
        Vector3 position = new Vector3();
        var ok = false;
        while (!ok)
        {
            ok = true;

            position = new Vector3(Random.Range(-X_WORLD_SIZE, X_WORLD_SIZE), 0.0F, Random.Range(-Z_WORLD_SIZE, Z_WORLD_SIZE));

            foreach (var obstacle in obstacles)
            {
                var distance = (position - obstacle.transform.position).magnitude;

                //assuming obstacle is a sphere just to simplify the point selection
                if (distance < obstacle.transform.localScale.x + AVOID_MARGIN)
                {
                    ok = false;
                    break;
                }
            }
        }

        return position;
    }

    private void Update()
    {
        if (Input.GetMouseButtonDown(0))
        {
            var mousePosition = Camera.main.ScreenToWorldPoint(new Vector3(Input.mousePosition.x, Input.mousePosition.y, 55.8F));
            mousePosition.y = 0;

            ActivateFollowMouse(mousePosition);
        }

        foreach (var character in this.Flock)
        {
            this.UpdateMovingGameObject(character);
        }

        if (IsFollowingMouse && FollowMouseMovement.Movement.Arrived.Count > FLOCK_ARRIVE_COUNT)
            DeactivateFollowMouse();
    }

    private void ActivateFollowMouse(Vector3 mousePosition)
    {
        FollowMouseMovement.Movement.Arrived.Clear();
        FollowMouseMovement.Movement.Target = new KinematicData();
        FollowMouseMovement.Movement.Target.position = mousePosition;
        FollowMouseMovement.Weight = MOUSE_CLICK_WEIGHT;
    }

    private void DeactivateFollowMouse()
    {
        FollowMouseMovement.Weight = 0;
        FollowMouseMovement.Movement.Target = null;
    }

    private void UpdateMovingGameObject(DynamicCharacter movingCharacter)
    {
        if (movingCharacter.Movement != null)
        {
            movingCharacter.Update();
            movingCharacter.KinematicData.ApplyWorldLimit(X_WORLD_SIZE, Z_WORLD_SIZE);
            movingCharacter.GameObject.transform.position = movingCharacter.Movement.Character.position;
        }
    }
}
