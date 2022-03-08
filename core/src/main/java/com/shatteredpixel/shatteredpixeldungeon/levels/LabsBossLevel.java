/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Bones;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Pylon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rebel;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Soldier;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.HandyBarricade;
import com.shatteredpixel.shatteredpixeldungeon.levels.builders.Builder;
import com.shatteredpixel.shatteredpixeldungeon.levels.builders.FigureEightBuilder;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.CityPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.SewerPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.RatKingRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sewerboss.GooBossRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sewerboss.SewerBossEntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sewerboss.SewerBossExitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.ImpShopRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

import java.util.ArrayList;
import java.util.HashSet;

public class LabsBossLevel extends Level {

	{
		color1 = 0x48763c;
		color2 = 0x59994a;
	}

	private static int WIDTH = 33;
	private static int HEIGHT = 34;

	private static final Rect entry = new Rect(0, 27, 33, 6);
	private static final Rect arena = new Rect(0, 0, 33, 26);

	private static final int topDoor = 16 + 33;
	private static final int bottomDoor = 16 + (arena.bottom+1) * 33;
	private static final int bossPos = bottomDoor-11*33;

	private static boolean isCompleted = false;

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_LABS;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_LABS;
	}

	@Override
	protected boolean build() {
		entrance = 16 + (30)*33;
		exit = topDoor;
		setSize(WIDTH, HEIGHT);

		//entrance room
		buildLevel();
		//arena room

		//customArenaVisuals = new LabArenaVisuals();
		//customArenaVisuals.setRect(0, 12, width(), 27);
		//customTiles.add(customArenaVisuals);

		return true;
	}

	private static final short n = -1; //used when a tile shouldn't be changed
	private static final short W = Terrain.WALL;
	private static final short e = Terrain.EMPTY;
	private static final short E = Terrain.ENTRANCE;
	private static final short p = Terrain.PEDESTAL;
	private static final short s = Terrain.EMPTY_SP;
	private static final short D = Terrain.DOOR;
	private static final short i = Terrain.CUSTOM_TILE_1;
	private static final short t = Terrain.CUSTOM_TILE_2;
	private static final short L = Terrain.LOCKED_EXIT;

	private static short[] level = {
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W,
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, L, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W,
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, i, i, i, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W,
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, i, p, i, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W,
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, i, i, i, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W,
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, i, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W,
			W, W, W, W, W, W, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, i, i, i, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, i, i, i, W, W,
			W, W, i, p, i, i, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, i, i, p, i, W, W,
			W, W, i, i, i, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, i, i, i, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, i, W, W, W, W, W, W,
			W, W, W, W, W, W, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, W, W, W, W, W, W,
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, D, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W,
			W, W, W, W, W, W, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, W, W, W, W, W, W,
			W, W, W, W, W, W, t, t, t, t, t, t, t, t, t, e, e, e, t, t, t, t, t, t, t, t, t, W, W, W, W, W, W,
			W, W, W, W, W, W, t, t, p, t, t, t, t, t, t, e, E, e, t, t, t, t, t, t, p, t, t, W, W, W, W, W, W,
			W, W, W, W, W, W, t, t, t, t, t, t, t, t, t, e, e, e, t, t, t, t, t, t, t, t, t, W, W, W, W, W, W,
			W, W, W, W, W, W, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, t, W, W, W, W, W, W,
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W
	};

	private void buildLevel(){
		int pos = 0 + 0*width(); 								//시작점의 x, y 좌표, width()가 곱해져 있는 것이 y다.

		short[] levelTiles = level; 							//위에서 노가다 한 것을 하나의 변수로 만들고
		for (int i = 0; i < levelTiles.length; i++){ 			//0부터 위 노가다에 포함된 요소의 개수만큼 반복
			if (levelTiles[i] != n) map[pos] = levelTiles[i];	//요소를 n으로 설정한 것이 아닌 경우 위 노가다에 포함
			// 된 요소대로 맵 제작
			pos++; 												//만들고 나서 다음 칸으로 이동하기 위해 필요한 것. ex)벽 -> 벽일 경우 첫번째 벽이 pos고, 두번째 벽이 pos+1.
		}
	}

	@Override
	public void occupyCell(Char ch) {

		super.occupyCell(ch);

		if (map[bottomDoor] != Terrain.LOCKED_DOOR && ch.pos < bottomDoor && ch == Dungeon.hero && !isCompleted) {

			seal();

		}
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
		drop( new HandyBarricade().quantity(5), 8+(30)*33 );
		drop( new HandyBarricade().quantity(5), 24+(30)*33 );
	}

	public int randomCellPos() {
		int eC_1 	= Random.IntRange( 7+7*33, 	25+7*33 );
		int eC_2 	= Random.IntRange( 7+8*33, 	25+8*33 );
		int eC_3 	= Random.IntRange( 7+9*33, 	25+9*33 );
		int eC_4 	= Random.IntRange( 7+10*33, 25+10*33 );
		int eC_5 	= Random.IntRange( 7+11*33, 25+11*33 );
		int eC_6 	= Random.IntRange( 7+12*33, 25+12*33 );
		int eC_7 	= Random.IntRange( 7+13*33, 25+13*33 );
		int eC_8 	= Random.IntRange( 7+14*33, 25+14*33 );
		int eC_9 	= Random.IntRange( 7+15*33, 25+15*33 );
		int eC_10 	= Random.IntRange( 7+16*33, 25+16*33 );
		int eC_11 	= Random.IntRange( 7+17*33, 25+17*33 );
		int eC_12 	= Random.IntRange( 7+18*33, 25+18*33 );
		int eC_13 	= Random.IntRange( 7+19*33, 25+19*33 );
		int eC_14 	= Random.IntRange( 7+20*33, 25+20*33 );
		int eC_15 	= Random.IntRange( 7+21*33, 25+21*33 );
		int eC_16 	= Random.IntRange( 7+22*33, 25+22*33 );
		int eC_17 	= Random.IntRange( 7+23*33, 25+23*33 );
		int eC_18 	= Random.IntRange( 7+24*33, 25+24*33 );
		int eC_19 	= Random.IntRange( 7+25*33, 25+25*33 ); //eC = emptyCell
		int randomEmptyCell = Random.oneOf( eC_1, eC_2, eC_3, eC_4, eC_5, eC_6, eC_7, eC_8, eC_9, eC_10, eC_11, eC_12, eC_13, eC_14, eC_15, eC_16, eC_17, eC_18, eC_19 );
		return randomEmptyCell;
	}


	@Override
	public void seal() {
		super.seal();

		Rebel boss = new Rebel();
		boss.state = boss.WANDERING;
		boss.pos = bottomDoor-11*33;
		GameScene.add( boss );
		boss.beckon(Dungeon.hero.pos);

		if (heroFOV[boss.pos]) {
			boss.notice();
			boss.sprite.alpha( 0 );
			boss.sprite.parent.add( new AlphaTweener( boss.sprite, 1, 0.1f ) );
		}

		//moves intelligent allies with the hero, preferring closer pos to entrance door
		int doorPos = bottomDoor;
		Mob.holdAllies(this, doorPos);
		Mob.restoreAllies(this, Dungeon.hero.pos, doorPos);

		set(bottomDoor, Terrain.LOCKED_DOOR);
		GameScene.updateMap(bottomDoor);
		Dungeon.observe();
	}

	@Override
	public void unseal() {
		super.unseal();

		set(topDoor, Terrain.UNLOCKED_EXIT);
		set(bottomDoor, Terrain.DOOR);
		GameScene.updateMap(topDoor);
		CellEmitter.get(topDoor).burst( Speck.factory( Speck.WOOL ), 8 );
		GameScene.updateMap(bottomDoor);
		isCompleted = true;

		Dungeon.observe();
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
			case Terrain.WATER:
				return Messages.get(LabsLevel.class, "water_name");
			case Terrain.WALL_DECO:
				return Messages.get(LabsLevel.class, "wall_deco_name");
			case Terrain.STATUE:
				return Messages.get(LabsLevel.class, "statue_name");
			case Terrain.CUSTOM_TILE_1:
				return Messages.get(LabsLevel.class, "custom_tile_1_name");
			case Terrain.CUSTOM_TILE_2:
				return Messages.get(LabsLevel.class, "custom_tile_2_name");
			case Terrain.CUSTOM_TILE_3:
				return Messages.get(LabsLevel.class, "custom_tile_3_name");
			case Terrain.LOCKED_EXIT:
				return Messages.get(LabsLevel.class, "locked_exit_name");
			case Terrain.UNLOCKED_EXIT:
				return Messages.get(LabsLevel.class, "unlocked_exit_name");
			default:
				return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc(int tile) {
		switch (tile) {
			case Terrain.ENTRANCE:
				return Messages.get(LabsLevel.class, "entrance_desc");
			case Terrain.EXIT:
				return Messages.get(LabsLevel.class, "exit_desc");
			case Terrain.EMPTY_DECO:
				return Messages.get(LabsLevel.class, "empty_deco_desc");
			case Terrain.WALL_DECO:
				return Messages.get(LabsLevel.class, "wall_deco_desc");
			case Terrain.BOOKSHELF:
				return Messages.get(LabsLevel.class, "bookshelf_desc");
			case Terrain.STATUE:
				return Messages.get(LabsLevel.class, "statue_desc");
			case Terrain.CUSTOM_TILE_1:
				return Messages.get(LabsLevel.class, "custom_tile_1_desc");
			case Terrain.CUSTOM_TILE_2:
				return Messages.get(LabsLevel.class, "custom_tile_2_desc");
			case Terrain.CUSTOM_TILE_3:
				return Messages.get(LabsLevel.class, "custom_tile_3_desc");
			case Terrain.LOCKED_EXIT:
				return Messages.get(LabsLevel.class, "locked_exit_desc");
			case Terrain.UNLOCKED_EXIT:
				return Messages.get(LabsLevel.class, "unlocked_exit_desc");
			default:
				return super.tileDesc( tile );
		}
	}
}