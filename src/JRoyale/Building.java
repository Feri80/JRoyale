public abstract class Building extends Card
{
    private final int hitSpeed;

    private final String target;

    private final double range;

    private final int initLifeTime;

    private final int[] initHP;

    public Building(String id, 
                    int cost, 
                    int hitSpeed, 
                    String target, 
                    double range, 
                    int initLifeTime, 
                    int[] initHP) 
    {
        super(id, cost);
        this.hitSpeed = hitSpeed;
        this.target = target;
        this.range = range;
        this.initLifeTime = initLifeTime;
        this.initHP = initHP;
    }

    public int getHitSpeed()
    {
        return hitSpeed;
    }

    public String getTarget()
    {
        return target;
    }

    public double getRange()
    {
        return range;
    }

    public int getInitLifeTime()
    {
        return initLifeTime;
    }

    public int getInitHP(int level)
    {
        return initHP[level];
    }

    @Override
    public void step(Creature creature) 
    {
        if(creature.getKillTarget() == null)
        {
            Creature target = creature.findNearestValidCreature();
            if(creature.isCreatureInRange(target))
            {
                creature.setKillTarget(target);
                creature.hit(creature.getKillTarget());
            }
        }
        else
        {
            if(creature.getKillTarget().isEliminated())
            {
                creature.setKillTarget(null);
            }
            else
            {
                if(creature.isCreatureInRange(creature.getKillTarget()))
                {
                    creature.hit(creature.getKillTarget());
                }
                else
                {
                    creature.setKillTarget(null);
                }
            }
        }
        
    }
}
