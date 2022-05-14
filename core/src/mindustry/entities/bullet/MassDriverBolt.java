package mindustry.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Nullable;
import mindustry.content.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.distribution.MassDriver.*;

import static mindustry.Vars.*;

public class MassDriverBolt extends BulletType{

    public MassDriverBolt(){
        super(1f, 75);
        collidesTiles = false;
        lifetime = 1f;
        despawnEffect = Fx.smeltsmoke;
        hitEffect = Fx.hitBulletBig;
        trailEffect = null;
    }

    @Override
    public void draw(Bullet b){
        float w = 11f, h = 13f;

        Draw.color(Pal.bulletYellowBack);
        Draw.rect("shell-back", b.x, b.y, w, h, b.rotation() + 90);

        Draw.color(Pal.bulletYellow);
        Draw.rect("shell", b.x, b.y, w, h, b.rotation() + 90);

        Draw.reset();

        //Draw trail
        if(b.trailEffect != null && b.timer(0, b.fslope() * 0.1f)){
            b.trailEffect.at(b.x, b.y);
        }
    }



    @Override
    public void update(Bullet b){
        //data MUST be an instance of DriverBulletData
        if(!(b.data() instanceof DriverBulletData data)){
            hit(b);
            return;
        }

        float hitDst = 7f;

        //if the target is dead, just keep flying until the bullet explodes
        if(data.to.dead()){
            return;
        }

        float baseDst = data.from.dst(data.to);
        float dst1 = b.dst(data.from);
        float dst2 = b.dst(data.to);

        boolean intersect = false;

        //bullet has gone past the destination point: but did it intersect it?
        if(dst1 > baseDst){
            float angleTo = b.angleTo(data.to);
            float baseAngle = data.to.angleTo(data.from);

            //if angles are nearby, then yes, it did
            if(Angles.near(angleTo, baseAngle, 2f)){
                intersect = true;
                //snap bullet position back; this is used for low-FPS situations
                b.set(data.to.x + Angles.trnsx(baseAngle, hitDst), data.to.y + Angles.trnsy(baseAngle, hitDst));
            }
        }

        //if on course and it's in range of the target
        if(Math.abs(dst1 + dst2 - baseDst) < 4f && dst2 <= hitDst){
            intersect = true;
        } //else, bullet has gone off course, does not get received.

        if(intersect){
            data.to.handlePayload(b, data);
        }
    }

    public Bullet create(@Nullable Entityc owner, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, boolean fireTrail, Object data){
        Bullet b = create(owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data);
        if(fireTrail)
            b.trailEffect = Fx.ballfire;
        return b;
    }

    @Override
    public void despawned(Bullet b){
        super.despawned(b);

        if(!(b.data() instanceof DriverBulletData data)) return;

        for(int i = 0; i < data.items.length; i++){
            int amountDropped = Mathf.random(0, data.items[i]);
            if(amountDropped > 0){
                float angle = b.rotation() + Mathf.range(100f);
                Fx.dropItem.at(b.x, b.y, angle, Color.white, content.item(i));
            }
        }
    }

    @Override
    public void hit(Bullet b, float hitx, float hity){
        super.hit(b, hitx, hity);
        despawned(b);
    }
}
