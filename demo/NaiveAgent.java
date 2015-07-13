/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.demo;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.LinkedList;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.VisionMBR;
import java.awt.image.*;

import javax.imageio.ImageIO;

public class NaiveAgent implements Runnable {

	private ActionRobot aRobot;
	private Random randomGenerator;
	public int currentLevel = 1;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	public TrajectoryPlanner tp;
	private boolean firstShot;
	private Point prevTarget;
	// a standalone implementation of the Naive Agent
	public NaiveAgent() {
		
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		randomGenerator = new Random();
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	
	// run the client
	public void run() {

		aRobot.loadLevel(currentLevel);
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = StateUtil.getScore(ActionRobot.proxy);
				if(!scores.containsKey(currentLevel))
					scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				int totalScore = 0;
				for(Integer key: scores.keySet()){

					totalScore += scores.get(key);
					System.out.println(" Level " + key
							+ " Score: " + scores.get(key) + " ");
				}
				System.out.println("Total Score: " + totalScore);
				aRobot.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("Restart");
				aRobot.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
				.println("Unexpected level selection page, go to the last current level : "
						+ currentLevel);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
				.println("Unexpected main menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
				.println("Unexpected episode menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}

		}

	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

public Point COM4(BufferedImage screenshot)
	{
		Vision vision = new Vision(screenshot);
		VisionMBR MBR = new VisionMBR(screenshot);					 
		List<ABObject> pigs = vision.findPigsMBR();
		List<Rectangle>  stones = MBR.findStonesMBR();
		List<Rectangle>  woods = MBR.findWoodMBR();
		List<Rectangle>  ice = MBR.findIceMBR();
		Point COM = new Point();
		double[] total_mass = new double[4];				
		double totalMass = 0;



		for(Rectangle stone :stones)
			total_mass[0] +=  stone.width*stone.height;
		//	COM.x += stone.width*stone.height*m_stone*(stone.getX());
		//	COM.y += stone.width*stone.height*m_stone*(stone.getY());
			
		


		for(Rectangle wood :woods)
			total_mass[1]	+= 	wood.width*wood.height;
		

		for(Rectangle ICE :ice)
			total_mass[2] += ICE.width*ICE.height;
		

		for(ABObject pig :pigs)
			total_mass[3] += pig.width*pig.height;
		
		int max_x = 0;

		for(ABObject pig :pigs)
			if(pig.getX()  > max_x)
				max_x =(int) pig.getX() ;
			
		
		

		double m_stone = (total_mass[0]/stones.size())/2;
		double m_wood  = (total_mass[1]/woods.size());
		double m_ice   = (total_mass[2]/ice.size());
		double m_pig   = 3*total_mass[3]/pigs.size();
		//System.out.println("Stone : " +m_stone + " Wood " + m_wood + " Ice "+ m_ice + " Pig " + m_pig);
		totalMass = 0;
		for(Rectangle stone :stones)
		{
			if(stone.getX() <= max_x)
			{
			COM.x += (int)/*stone.width*stone.height*/m_stone*(stone.getX());
			COM.y += (int)/*stone.width*stone.height*/m_stone*(stone.getY());
			totalMass += /*stone.width*stone.height*/m_stone;
			}
		}

		for(Rectangle wood :woods)
		{
			if(wood.getX() <= max_x)
			{
			COM.x += (int)/*wood.width*wood.height*/m_wood*(wood.getX());
			COM.y += (int)/*wood.width*wood.height*/m_wood*(wood.getY());
			totalMass += /*wood.width*wood.height*/m_wood;
			}
		}

		for(Rectangle ICE :ice)
		{
			if(ICE.getX() <= max_x)
			{
			COM.x += (int)/*ICE.width*ICE.height*/m_ice*(ICE.getX()) ;
			COM.y += (int)/*ICE.width*ICE.height*/m_ice*(ICE.getY());
			totalMass += /*ICE.width*ICE.height*/m_ice;
			}
		}

		for(ABObject pig :pigs)
		{
			COM.x += (int)/*pig.width*pig.height*/m_pig*(pig.getX() );
			COM.y += (int)/*pig.width*pig.height*/m_pig*(pig.getY());
			totalMass +=  /*pig.width*pig.height*/m_pig;
		}


		
		COM.x = (int)COM.getX()/(int)totalMass;
		COM.y = (int)COM.getY()/(int)totalMass;
		return COM;
}

	public Point COM2(BufferedImage screenshot, int mass[])
	{
		Vision vision = new Vision(screenshot);
		VisionMBR MBR = new VisionMBR(screenshot);					 
		List<ABObject> pigs = vision.findPigsMBR();
		 
		int m_wood = mass[0];
		int m_ice = mass[1];
		int m_stone = mass[2];
		int m_pig = mass[3];
										
			List<Rectangle>  stones = MBR.findStonesMBR();
			List<Rectangle>  woods = MBR.findWoodMBR();
			List<Rectangle>  ice = MBR.findIceMBR();
			Point COM = new Point();
			int total_mass = 0;				




			for(Rectangle stone :stones)
			{
				COM.x += stone.width*stone.height*m_stone*(stone.getX());
				COM.y += stone.width*stone.height*m_stone*(stone.getY());
				total_mass +=  stone.width*stone.height*m_stone;
			}


			for(Rectangle wood :woods)
			{
				COM.x += wood.width*wood.height*m_wood*(wood.getX());
				COM.y += wood.width*wood.height*m_wood*(wood.getY());
				total_mass	+= 	wood.width*wood.height*m_wood;
						}

			for(Rectangle ICE :ice)
			{
				COM.x += ICE.width*ICE.height*m_ice*(ICE.getX()) ;
				COM.y += ICE.width*ICE.height*m_ice*(ICE.getY());
				total_mass += ICE.width*ICE.height*m_ice;
			}

			for(ABObject pig :pigs)
			{
				COM.x += pig.width*pig.height*m_pig*(pig.getX() );
				COM.y += pig.width*pig.height*m_pig*(pig.getY());
				total_mass += pig.width*pig.height*m_pig;
			}


			
			COM.x = (int)COM.getX()/total_mass;
			COM.y = (int)COM.getY()/total_mass;
			return COM;
}




	public int Compute_Tap_Interval(BufferedImage screenshot, Point pt, Rectangle sling, Point _tpt)
	{
		Vision vision = new Vision(screenshot);
		VisionMBR MBR = new VisionMBR(screenshot);					 
		List<ABObject> pigs = vision.findPigsMBR();
		List<ABObject> blocks = MBR.findBlocks();
		System.out.println("IN TAP " + _tpt.x);
		int min_x = MBR._nWidth;
		for(ABObject blk : blocks)
		{
			if(blk.getX() < min_x)
			{
				min_x = (int)blk.getX();
			}
		}
		List<Point> trajectory = tp.predictTrajectory(sling, pt);
		
		List<Point> traj_to_target = new LinkedList<Point>();
			int k= 0;
			double per = 0.15;
			switch (aRobot.getBirdTypeOnSling()) 
						{

						case RedBird:
							per = 0; break;    
						case YellowBird:
							per = 0.15 ; break; 
						case BlueBird:
							per = 0.15;  break; 
						default:
							per =  0.15;
						}
			for(k= 0; k<trajectory.size(); k++)
			{

						if(trajectory.get(k).getX() >= min_x)
							break;
			}
			int p = k;
			for(p = k ; p<trajectory.size(); p++)
			{
				int jk = 0;
						for(ABObject blk : blocks)
						{
							if(trajectory.get(p).getX() >= blk.getX() && trajectory.get(p).getX() <= blk.getX() + blk.width && trajectory.get(p).getY() >= blk.getY() && trajectory.get(p).getY() <= blk.getY() + blk.height)
							{			
								jk = 1;
	
										break;
							}
					}
					if(jk == 1)
					{
						break;
					}
			}

			Point tapPoint = new Point();
			if(p == trajectory.size())
			{
				tapPoint.x  =(int) _tpt.x;
				tapPoint.y  = (int)  _tpt.y;
				tapPoint.x -= per*(tapPoint.x - sling.x);
			}
			else
			{
			System.out.println("Tap Point: " + tapPoint.x + "  " + tapPoint.y);
			tapPoint.x  =(int) trajectory.get(p).getX();
			tapPoint.y  = (int)  trajectory.get(p).getY();
			tapPoint.x -= per*(tapPoint.x - sling.x);
			}
			double tap_interval = ((double)traj_to_target.size()/(double)trajectory.size())*100;
			System.out.println(" TAP : " + (int)tap_interval);
			int taptime = tp.getTimeByDistance(sling, pt, tapPoint);
			System.out.println(" TAP : " + taptime);
				return taptime;
}

public ABObject Compute_Top(BufferedImage screenshot)
	{	Vision vision = new Vision(screenshot);
		VisionMBR MBR = new VisionMBR(screenshot);					 
		List<ABObject> pigs = vision.findPigsMBR();
		List<ABObject> blocks = MBR.findBlocks();
	
		List<ABObject> TOPS = new LinkedList<ABObject>();
		int N = MBR._nHeight;
		ABObject top = new ABObject();
		ABObject temp = new ABObject();
		top.y = 0;
		for(Rectangle p : pigs)
		{
			//if(b.type.id != 12)
			temp.y = 0;
			for(ABObject b : blocks)
			{
				if(b.getY() < p.getY() &&(b.getX()  <= p.getX()) && ((b.getX() + b.width) >= (p.getX() + p.width)))
					if(b.getY() > temp.getY())
							temp = b;
					else if (b.getY() == temp.getY())
						if(b.width > temp.width)
							temp = b;		
						}
			if(temp.y != 0)
			TOPS.add(temp);
			if(temp.getY() >= top.getY())
				{	
					top = temp;
				}	
			}
		
		
		System.out.println("No of tops " + TOPS.size());
		/*for(ABObject bas : TOPS)
		{

			System.out.println(bas.type + "   " + bas.width + "   " + bas.height + "    " +  bas.getX() + "   " + bas.getY());
		}*/
		if(TOPS.size() == 0)
			return null;

		return top;
	}

public ABObject Compute_Base(BufferedImage screenshot)
	{
		Vision vision = new Vision(screenshot);
		VisionMBR MBR = new VisionMBR(screenshot);					 
		List<ABObject> pigs = vision.findPigsMBR();
		List<ABObject> blocks = MBR.findBlocks();
					
		int M = MBR._nWidth;
		ABObject base = new ABObject();
		base.x = MBR._nWidth;
		base.y = 0;

		List<ABObject> BASES = new LinkedList<ABObject>();
		int flg = 0;
		for(ABObject b : blocks)
		{
			if(b.type.id != 12)
			for(Rectangle p : pigs)
			{
				if(((b.getY() <= p.getY() + p.height + 8) && (b.getY() >= p.getY() + p.height)) &&((((b.getX() + b.width) >= p.getX()) && ((b.getX() + b.width) <= (p.getX() + p.width)))|| (b.getX() >= p.getX() && b.getX() <= p.getX() + p.width) || (b.getX() <= p.getX() && b.getX() + b.width >= p.getX() + p.width)))// - 4*p.width) && (b.getX() <= p.getX() + 4*p.width)))
					{	BASES.add(b);
						if(b.getX() <= M )
						{	flg = 1;
							if(b.getX() == M)
							{
								if(b.getY()  > base.getY())
									base = b;
							}
							else
							{	
							M =(int) b.getX();
							base = b;
							}
						}
					}
			}

		}

			/*for(ABObject bas : BASES)
			{
				System.out.println(bas.type + "   " + bas.width + "   " +  bas.getX() + "   " + bas.getY());
			}*/

			
			for(ABObject bas : BASES)
			{
				flg = 1;
				for(ABObject blk : blocks)
				{
					if(blk.getX() < base.getX())
					{
						if(blk.getY() <= base.getY() && (blk.getY()  + blk.height) >= base.getY())
							{
								System.out.println("OBSTACLE! " + blk.type + "   "+  blk.height +  "  at " + blk.getX() + "  ,  "  + blk.getY());
								flg = 0;
							}
					}
						}
				if(flg == 1 )
				{	if(bas.getX() < base.getX())
						{
						base = bas;
						}
					else if(bas.getX() == base.getX())
					{
						if(bas.getY() > base.getY())
							base = bas;
					}
				}
			}
			if(flg == 0)
				return null;
			return base;
	}

public Point COM_Check(BufferedImage screenshot, Point COM)
	{
		Vision vision = new Vision(screenshot);
		VisionMBR MBR = new VisionMBR(screenshot);					 
		List<ABObject> pigs = vision.findPigsMBR();
		
		ABObject closest_pig = new ABObject();
		int y = (randomGenerator.nextInt(pigs.size()));
		Point  P = new Point();
			P.x = (int)pigs.get(y).getCenter().getX();
			P.y = (int)pigs.get(y).getCenter().getY();
			closest_pig = pigs.get(y);


			// pick the max height pig
		double min = distance(COM, P);
		//System.out.println("min" + min);
		
		for(ABObject pig : pigs)
		{
			P.x = (int)pig.getCenter().getX();
			P.y = (int)pig.getCenter().getY();
			if(distance(COM, P) <= min)
			{
				closest_pig = pig;
				min = distance(COM, P);
			}
		
		}
		
		Point _tpt = new Point();	
		ABObject pig;
		if(min > 100)
		{
		  pig = closest_pig;
		 _tpt = pig.getCenter();/// if the target is very close to before, randomly choose a
		}
		// point near it
		else
		{
		_tpt = COM;
		}
			
		return _tpt;
}

public int Select_Trajectory(BufferedImage screenshot, Point _tpt)
{
	Vision vision = new Vision(screenshot);
	VisionMBR MBR = new VisionMBR(screenshot);					 
	List<ABObject> blocks = MBR.findBlocks();
	int flag = 0;
	for(ABObject block : blocks)
		{
			if(block.getCenter().getX() <= _tpt.x)
			{	
				if(block.getY() < _tpt.y && Math.abs(block.getY() - _tpt.y) > 110) 
				{
				//	System.out.println("BLOCKING " + block.type +"  : "+  block.getX() + "   " + block.getY() + "DIff " + Math.abs(block.getCenter().getY() - _tpt.y));
					flag = 1;
					break;
				}
			}
		 }

	return flag;
					 
}
	public GameState solve()
	{

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		// find the slingshot
		Rectangle sling = vision.findSlingshotMBR();

		// confirm the slingshot
		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out
			.println("No slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}
        // get all the pigs
	 		List<ABObject> pigs = vision.findPigsMBR();

		GameState state = aRobot.getState();



		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty()) {


				Point releasePoint = null;
				Shot shot = new Shot();
				int dx,dy;
				{
					VisionMBR MBR = new VisionMBR(screenshot);					 

					Point COM = new Point();
					COM.x = 0;
					COM.y = 0;
					
					List<ABObject> blocks = MBR.findBlocks();
					ABObject base = Compute_Base(screenshot);
					int flg1 = 0;
					
					if(base != null)
						for(ABObject blk : blocks)
						if((base.getX() + base.width + 5 >= blk.getX()) && (base.getX() + base.width <= blk.getX()) || (blk.getX() >= base.getX() && blk.getX()  + blk.width <= base.getX() + base.width && blk.getY() < base.getY()))
							{flg1 = 1;
							break;	}
					

					ABObject top = Compute_Top(screenshot);
					int flg2 = 0;
					if(top != null)
						flg2 = 1;
					
					//System.out.println("FLg 1 :" + flg1 + "FLG 2 : "+ flg2);
					if(flg1 == 1 && flg2 == 1)
					{
					//	System.out.println("BASE " + base.width + "  " + "TOP "+ top.width);
						if(base.width <= top.width)
							{
								 flg2 = 0;
						//		System.out.println("Select base over top");
							}
					}
					if(flg2 == 1)
					{
						COM.x = (int)top.getX();
						COM.y = (int)top.getY() + top.height;
						System.out.println(top.type + "   " + COM.x  + "   ---TOP---   " + COM.y);
					}
					else if(flg1 == 1)
					{	COM.x = (int)base.getX();
						COM.y = (int)base.getY();
						//System.out.println(base.type + "   " + COM.x  + "   ---BASE---   " + COM.y);
						}
					else 
					{
						
						Point[] coms = new Point[5];
						int[] mass = new int[4];
						mass[0] = 5;
						mass[1] = 4;
						mass[2] = 3;
						mass[3] = 11;
						COM= COM4(screenshot);
					//System.out.println("com : " + COM.x + "    --   " + COM.y);
					}
					
					
					Point _tpt = COM_Check(screenshot, COM);
					
					System.out.println("target :" + _tpt.x + "   " + _tpt.y);
					
					int flag = Select_Trajectory(screenshot, _tpt);
					
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = randomGenerator.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}

					prevTarget = new Point(_tpt.x, _tpt.y);
					
					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);
					
					if(!pts.isEmpty())
					{
						if(flag == 0)
						releasePoint = pts.get(0);
						else	
						{	/*else*/
							 if (pts.size() == 1)
								releasePoint = pts.get(0);
							else if (pts.size() == 2)
							
										releasePoint = pts.get(1);
								}
									}
					else
					{
							System.out.println("No release point found for the target");
							System.out.println("Try a shot with 45 degree");
							releasePoint = tp.findReleasePoint(sling, Math.PI/4);
						}
					/*
					 List<Point> trajectory = tp.predictTrajectory(sling, releasePoint);
					for(int k= 0; k<trajectory.size(); k++)
					{
						System.out.println(trajectory.get(k).getX() + " " + trajectory.get(k).getY());
						System.out.println(screenshot.getRGB(trajectory.get(k).getY(), trajectory.get(k).getX()));
						}*/
					// Get the reference point
					Point refPoint = tp.getReferencePoint(sling);


					//Calculate the tapping time according the bird type 
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println("Release Point: " + releasePoint);
						System.out.println("Release Angle: "
								+ Math.toDegrees(releaseAngle));
						System.out.println("HI");
						int tapTime = Compute_Tap_Interval(screenshot, releasePoint,sling, _tpt );
						
						/*
						switch (aRobot.getBirdTypeOnSling()) 
						{
						case RedBird:
							tapInterval = 0; break;               // start of trajectory
						case YellowBird:
							tapInterval = 65 + randomGenerator.nextInt(25);break; // 65-90% of the way
						case WhiteBird:
							tapInterval =  70 + randomGenerator.nextInt(10);break; // 70-90% of the way
						case BlackBird:
							tapInterval =  70 + randomGenerator.nextInt(10);break; // 70-90% of the way
						case BlueBird:
							tapInterval =  60 + randomGenerator.nextInt(10);break; // 60-70% of the way
						default:
							tapInterval =  60;
						}
						*/
						//int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						dx = (int)releasePoint.getX() - refPoint.x;
						dy = (int)releasePoint.getY() - refPoint.y;
						shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
					}
					else
						{
							System.err.println("No Release Point Found");
							return state;
						}
				}

				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				{
					ActionRobot.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					if(_sling != null)
					{
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						if(scale_diff < 25)
						{
							if(dx < 0)
							{
								aRobot.cshoot(shot);
								state = aRobot.getState();
								if ( state == GameState.PLAYING )
								{
									screenshot = ActionRobot.doScreenShot();
									vision = new Vision(screenshot);
									List<Point> traj = vision.findTrajPoints();
									tp.adjustTrajectory(traj, sling, releasePoint);
									firstShot = false;
								}
							}
						}
						else
							System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
					}
					else
						System.out.println("no sling detected, can not execute the shot, will re-segement the image");
				}

			}

		}
		return state;
	}

	public static void main(String args[]) {
		
		NaiveAgent na = new NaiveAgent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}