import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class contains a creature on the arena 
 */
public class Creature 
{
    /**
     * the card that creature based on that
     */
    private Card card;

    /**
     * the level of the creature
     */
    private int level;

    /**
     * the center position of the creature 
     */
    private Point2D position;

    /**
     * the current image that creture shown based on status
     */
    private ImageView image;

    /**
     * the side that creature fights for
     * -1 -> belongs to bot , 1 -> belongs to player 
     */
    private int side; 

    /**
     * the current status of the creature
     * 0 -> created recently , 1 -> move to right , 2 -> move to left , 3 -> fighting to right , 4 -> fighting to left , 5 -> dying to right , 6 -> dying to left
     */
    private int status;

    /**
     * the creature this creature should follow
     */
    private Creature followTarget;

    /**
     * the creature that this creature should kill or be killed
     */
    private Creature killTarget;

    /**
     * the current hp of creature
     */
    private int hp;

    /**
     * the current life time of the creature
     */
    private int lifeTime;

    /**
     * the tick time to hit
     */
    private int hitStepValue;

    private int hitSpeed;

    private int speed;

    private boolean underRage;

    private int damage;

    private int rageTimeRemained;//in milliseconds

    /**
     * demonstrate the creature status toward the bridges in the map from the player vision//0->king or princess//down bridge is the nearer bridge//1->before bridge//2->on the bridge//3->after the bridge//top bridge is the nearer bridge//4->before bridge//5->on the bridge//6->after the bridge
     * */
    private int bridgeStatus;

    /**
     * creates a new creature
     * @param card
     * @param position
     * @param side
     */
    public Creature(Card card, int level, Point2D position, int side) 
    {
        this.card = card;
        this.level = level;
        this.position = position;
        this.side = side;

        underRage = false;
        rageTimeRemained = 0;

        speed = card.getSpeed();

        hitSpeed = card.getHitSpeed();
        hitStepValue = 0;

        damage = card.getDamage(level);

        updateBridgeStatus();

        if(card instanceof Building)
        {
            this.hp = ((Building)card).getInitHP(level);
            this.lifeTime = ((Building)card).getInitHP(level);
        }
        else if(card instanceof Troop)
        {
            this.hp = ((Troop)card).getInitHP(level);
            this.lifeTime = 1000000;
        }
    }

    public int getSide() 
    {
        return side;
    }

    public int getLevel() 
    {
        return level;
    }

    public boolean isUnderRage() 
    {
        return underRage;
    }

    public Card getCard() 
    {
        return card;
    }

    public int getRageTimeRemained() 
    {
        return rageTimeRemained;
    }

    public Point2D getPosition() 
    {
        return position;
    }

    public ImageView getImage() 
    {
        return image;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public int updateBridgeStatus() 
    {
        if(position.getY() > 360)
        {
            if(position.getX() < 600)
                bridgeStatus = 1;
            else if(position.getX() >= 600 && position.getX() <= 680)
                bridgeStatus = 2;
            else
                bridgeStatus = 3;
        }
        else
        {
            if(position.getX() < 600)
                bridgeStatus = 4;
            else if(position.getX() >= 600 && position.getX() <= 680)
                bridgeStatus = 5;
            else
                bridgeStatus = 6;
        }
        if(card instanceof King || card instanceof Princess)
            bridgeStatus = 0;

        return bridgeStatus;
    }

    public void setPosition(Point2D position) 
    {
        this.position = position;
    }

    public Creature getKillTarget()
    {
        return killTarget;
    }

    public void setKillTarget(Creature creature)
    {
        this.killTarget = creature;
    }

    public Creature getFollowTarget()
    {
        return followTarget;
    }

    public int getStatus() {
        return status;
    }

    public void setUnderRage(boolean newUnderRage)
    {
        this.underRage = newUnderRage;
    }

    public void setRageTimeRemained(int rageTimeRemained) 
    {
        this.rageTimeRemained = rageTimeRemained;
    }

    public void setFollowTarget(Creature creature)
    {
        this.followTarget = creature;
    }

    public int getHP()
    {
        return hp;
    }

    public void step()
    {
        card.step(this);

        if(underRage)
        {
            rageTimeRemained -= 1000 / GameManager.FPS;

            if(rageTimeRemained == 0)
                underRage = false;
        }
    }

    /**
     * this creature get hit
     * @param damage
     * @return true if target eliminated else false
     */
    public boolean getHit(int damage, int tempStatus)
    {
        hp -= damage;
        if(hp <= 0)
        {
            status = tempStatus;

            hp = 0;
            return true;
        }
        return false;
    }

    /**
     * hit another creature
     * @param creature
     * @return true if target creature eliminated else false
     */
    public boolean hit(Creature creature)
    {
        if(hitStepValue >= hitSpeed) 
        {
            if(card instanceof Troop && ((Troop) card).isAreaSplash()) {
                if(card instanceof Valkyrie){
                    for(Creature c : GameManager.getInstance().getBattle().getArena().getCreatures())
                        if(c.getPosition().distance(position) < 40 && c.getSide() != side && (c.getCard().getType().equals("ground") || c.getCard().getType().equals("building")))
                            c.getHit((int) (damage * (underRage ? 1.4 : 1)), position.getX() < c.position.getX() ? 6 : 5);
                }
                else{
                    for(Creature c : GameManager.getInstance().getBattle().getArena().getCreatures())
                        if(c.getPosition().distance(creature.position) < 40 && c.getSide() != side)
                            c.getHit((int) (damage * (underRage ? 1.4 : 1)), position.getX() < c.position.getX() ? 6 : 5);
                }
            }
            else
                creature.getHit((int) (damage * (underRage ? 1.4 : 1)), position.getX() < creature.position.getX() ? 6 : 5);

            hitStepValue = 0;
        }
        else
            hitStepValue += hitSpeed / GameManager.FPS;

        return creature.isEliminated();
    }

    /**
     * @return is creature eliminated
     */
    public boolean isEliminated()
    {
        if(hp == 0)
        {
            return true;
        }
        return false;
    }

    public int getDistance(Creature creature)
    {
        return (int)position.distance(creature.position);
    }

    public Creature findNearestValidCreature()
    {
        return card.findNearestValidCreature(this);
    }

    public boolean isCreatureInRange(Creature creature)
    {
        if(creature != null){
            double distance = creature.getPosition().distance(position);

            if (distance <= card.getRange() * 40 || (creature.getCard() instanceof King && distance <= (card.getRange() + 2) * 40) || (creature.getCard() instanceof Princess && distance <= (card.getRange() + 1.5) * 40) || ((creature.getCard() instanceof Inferno || creature.getCard() instanceof Cannon) && distance <= (card.getRange() + 1) * 40))
                return true;
        }
        return false;
    }

    public void followCreature(Creature creature)
    {
        for(int i = 0;i < calculateRageEffect(speed);i++) {
//            System.out.println("position :" + position.getX() + ", " + position.getY());
            //System.out.println(card.getId() + " is moving");
            if (card instanceof Troop)
                pixelMove();
        }
    }

    private int calculateRageEffect(int speed) {
        if(underRage && (rageTimeRemained % 200 == 0))
        {
            return 3 * speed;
        }
        else
            return speed;
    }

    private void pixelMove()
    {// System.out.println("pixel move working346");
        ArrayList<Point2D> probablePositions = new ArrayList<>();

        if(notInViewRange(position.add(side, 0)))
            probablePositions.add(position.add(side, 0));
        if(notInViewRange(position.add(side, -1)))
            probablePositions.add(position.add(side, -1));
        if(notInViewRange(position.add(side, 1)))
            probablePositions.add(position.add(side, 1));
        if(notInViewRange(position.add(0, 1)))
            probablePositions.add(position.add(0, 1));
        if(notInViewRange(position.add(0, -1)))
            probablePositions.add(position.add(0, -1));
        if(notInViewRange(position.add(side * -1, -1)))
            probablePositions.add(position.add(side * -1, -1));
        if(notInViewRange(position.add(side * -1, 0)))
            probablePositions.add(position.add(side * -1, 0));
        if(notInViewRange(position.add(side * -1, 1)))
            probablePositions.add(position.add(side * -1, 1));

        Point2D tempTargetPosition = findTempTargetPosition();
        probablePositions = inRangePoints(probablePositions);

        if(probablePositions.size() != 0) {//System.out.println("moving successfully369");
            Point2D newPosition = findNearestPosition(tempTargetPosition, probablePositions);
            setPositionAndStatus(newPosition);//System.out.println("moving successfully finished?!?!?!?!?!?371");
        }
        else
        {
            ArrayList<Point2D> allPositions = new ArrayList<>();

            allPositions.add(position.add(side, 0));
            allPositions.add(position.add(side, 1));
            allPositions.add(position.add(side, -1));
            allPositions.add(position.add(0, 1));
            allPositions.add(position.add(0, -1));
            allPositions.add(position.add(side * -1, -1));
            allPositions.add(position.add(side * -1, 0));
            allPositions.add(position.add(side * -1, 1));

            allPositions = inRangePoints(allPositions);

            Point2D newPosition = findNearestPosition(tempTargetPosition, allPositions);
            setPositionAndStatus(newPosition);
//            System.out.println("moving struggle390");
            moveCreaturesBackward(findInViewRangeCreatures(position));
        }
    }
    /**
     * sets the new position and update the status
     * @param newPosition to set
     * */
    private void setPositionAndStatus(Point2D newPosition) 
    {
            if (newPosition.getX() < position.getX())
                status = 2;
            else {
                status = 1;
            }
            position = newPosition;
    }

    private Point2D findTempTargetPosition() 
    {
        Creature target;
        if(killTarget == null){
            if(followTarget == null)
                target = followTarget = findNearestValidCreature();
            else
                target = findNearestValidCreature();
        }
        else
            target = killTarget;
//        System.out.println("finding temp target position417");
        int enemyBridgeStatus = target.updateBridgeStatus();
        bridgeStatus= updateBridgeStatus();

        if(enemyBridgeStatus == bridgeStatus || (enemyBridgeStatus == 1 && bridgeStatus == 4) || (enemyBridgeStatus == 4 && bridgeStatus == 1) || (enemyBridgeStatus == 3 && bridgeStatus == 6) || (enemyBridgeStatus == 6 && bridgeStatus == 3) || card instanceof Dragon){
            return target.position;
        }
        else if((bridgeStatus == 1 || bridgeStatus == 4) && side == 1){
            if((position.getY() <= 620 && position.getY() >= 540) || (position.getY() <= 180 && position.getY() >= 100))
                return target.getPosition().distance(600, 140) < target.getPosition().distance(600, 580) ? new Point2D(600, position.getY()) : new Point2D(600, position.getY());

            Point2D bridge = target.getPosition().distance(600, 140) < target.getPosition().distance(600, 580) ? new Point2D(600, 140) : new Point2D(600, 580);

            if(position.getY() < bridge.getY())
                return new Point2D(bridge.getX(), bridge.getY() - 20);
            else
                return new Point2D(bridge.getX(), bridge.getY() + 20);
        }
        else if((bridgeStatus == 3 || bridgeStatus == 6) && side == -1){
            if((position.getY() <= 620 && position.getY() >= 540) || (position.getY() <= 180 && position.getY() >= 100))
                return target.getPosition().distance(680, 140) < target.getPosition().distance(680, 580) ? new Point2D(680, position.getY()) : new Point2D(680, position.getY());

            Point2D bridge = target.getPosition().distance(680, 140) < target.getPosition().distance(680, 580) ? new Point2D(680, 140) : new Point2D(680, 580);

            if(position.getY() < bridge.getY())
                return new Point2D(bridge.getX(), bridge.getY() - 20);
            else
                return new Point2D(bridge.getX(), bridge.getY() + 20);
        }
        else if(bridgeStatus == 2 || bridgeStatus == 5){
            if((side == 1 || side == -1) && target.getPosition().getX() > position.getX()) {System.out.println("side 1 and bridge status = 5 || 2");
                return position.distance(target.position) < position.distance(new Point2D(680, target.position.getY())) ? target.position : new Point2D(680, target.position.getY());
            }
            else {System.out.println("side -1 and bridge status = 5 || 2");
                return position.distance(target.position) < position.distance(new Point2D(600, target.position.getY())) ? target.position : new Point2D(600, target.position.getY());
            }
        }
        return target.position;
    }

    private void moveCreaturesBackward(ArrayList<Creature> inRanges) 
    {
        for(Creature c : inRanges)
        if(!(c.getCard() instanceof Building))
        {//System.out.println("moving other creatures backward458");
            ArrayList<Point2D> probablePositions = new ArrayList<>();

            if (notInViewRange(c.getPosition().add(side, 0)))
                probablePositions.add(c.getPosition().add(side, 0));
            if (notInViewRange(c.getPosition().add(side, -1)))
                probablePositions.add(c.getPosition().add(side, -1));
            if (notInViewRange(c.getPosition().add(side, 1)))
                probablePositions.add(c.getPosition().add(side, 1));
            if (notInViewRange(c.getPosition().add(0, 1)))
                probablePositions.add(c.getPosition().add(0, 1));
            if (notInViewRange(c.getPosition().add(0, -1)))
                probablePositions.add(c.getPosition().add(0, -1));
            if (notInViewRange(c.getPosition().add(side * -1, -1)))
                probablePositions.add(c.getPosition().add(side * -1, -1));
            if (notInViewRange(c.getPosition().add(side * -1, 0)))
                probablePositions.add(c.getPosition().add(side * -1, 0));
            if (notInViewRange(c.getPosition().add(side * -1, 1)))
                probablePositions.add(c.getPosition().add(side * -1, 1));

            Point2D tempTargetPosition = (killTarget == null ? followTarget : killTarget).getPosition();
            probablePositions = inRangePoints(probablePositions);

            if (probablePositions.size() != 0) {//System.out.println("moving creature backward successfully481");
                Point2D newPosition = findNearestPosition(tempTargetPosition, probablePositions);
                setPositionAndStatus(newPosition);
            }
        }
    }

    private ArrayList<Creature> findInViewRangeCreatures(Point2D position) 
    {
        ArrayList<Creature> inRange = new ArrayList<>();
        Iterator<Creature> it = GameManager.getInstance().getBattle().getArena().getCreatures().iterator();
//        System.out.println("finding creatures in view overlap492");
        while (it.hasNext())
        {
            Creature temp = it.next();

            if(temp != this && position.distance(temp.position) < 10)
                inRange.add(temp);
        }
        return inRange;
    }

    private Point2D findNearestPosition(Point2D source, ArrayList<Point2D> positions)
    {
        Iterator<Point2D> it = positions.iterator();
        Point2D newPosition = positions.get(0);
//        System.out.println("finding the nearest position to a source507");
        while (it.hasNext())
        {
            Point2D tempPosition = it.next();

            if(!(source.getY() == tempPosition.getY() && source.getX() == tempPosition.getX()) && source.distance(newPosition) > source.distance(tempPosition))
                newPosition = tempPosition;
        }
        return newPosition;
    }

    private boolean notInViewRange(Point2D newPosition)
    {
        Iterator<Creature> it = GameManager.getInstance().getBattle().getArena().getCreatures().iterator();
//        System.out.println("checking the creature position accuracy521");
        while (it.hasNext()){
            Creature c = it.next();

            if(c != this && !(c.card instanceof Spell) && newPosition.distance(c.position) < 10){
                return false;
            }
        }
        return true;
    }

    public ArrayList<Point2D> inRangePoints(ArrayList<Point2D> points)
    {
        ArrayList<Point2D> validates = new ArrayList<>();
        Iterator<Point2D> it = points.iterator();
//        System.out.println("finding in range positions536");
        while (it.hasNext())
        {
            Point2D temp = it.next();
            double x = temp.getX();
            double y = temp.getY();

            if(y < 710 && x < 1270 && x > 10 && y > 10)
            {
                if(x < 680 && x > 600){
                    if((y >= 90 && y <= 190) || (y >= 530 && y <= 610))
                        validates.add(temp);
                }
                else
                    validates.add(temp);
            }
        }
        return validates;
    }
}