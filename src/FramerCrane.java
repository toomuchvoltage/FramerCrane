//********************************************************************
//   **************************************************************          
//        ****************************************************     
//                 **********************************                               
//                           **************                               
//           FramerCrane.java by Baktash Abdollah-Shamshirsaz
//                           **************                               
//                 **********************************                               
//        ****************************************************     
//   **************************************************************          
//********************************************************************
//
//http://3rdprogress.no-ip.com
import java.applet.*;
import java.lang.*;
import java.awt.*;
import java.io.*;
import java.math.*;

public class FramerCrane extends Applet
{
    float dist = 5; // Distance from eye to origin!
    public static vector3d eye = new vector3d (0, 0, 5), look = new vector3d (0, 0, 0); // The eye point (viewer's head) and the look point (The point we're looking at(here it's the origin))
    float lookdeg_x = 0.0f, lookdeg_y = 0.0f; // The rotation degree of eye-look vector around the x and y axis!
    public static zbuffer This_zbuff; // The ZBuffer of the 3d view

    boolean first_time = true; // true if we're start dragging the mouse in a window!
    float mousex1, mousey1, mousex2, mousey2; // The mouse's 1st and 2nd position!

    public static Graphics _3d_gr, _left_gr, _front_gr, _top_gr; // The Graphics handlers for the 4 views
    Image _3d_img, _left_img, _top_img, _front_img; // The four images for out four views
    public static int width, height; // The width and height of every view

    int window = 0; // The window selected (Mouse dragged in)
    Graphics applet_gr; // The applet's graphic handler
    boolean error = false; // true = Error happend...

    triangle map [] = new triangle [100000]; // Our Whole Simple Map!!!
    int n_map = 0; // The number of polygons in the map
    String filename; // Name of the file to be loaded/saved
    boolean getfile = false;
    int save_load = 0; // If 1, then we want to load file,if 2 then we want to save it...if none then we want to do nothing

    Color current_color; // The color of the currently drawn object
    float depth = 1; // The depth of the object we're drawing
    int type = 1; // Type of object we're drawing (1 = box,2 = prism,3 = cylinder,...)

    public void save ()  // Save map from the specified file in "filename"
    {
	save_load = 0;
	try // We need to watch for IOExceptions
	{
	    FileOutputStream save_file = new FileOutputStream (filename.substring (0, filename.length ()));
	    PrintStream writer = new PrintStream (save_file); // We use an print stream so we can write lines easily...
	    for (int count = 0 ; count != n_map ; count++)
	    {
		vector3d v1_w, v2_w, v3_w;
		Color col_w;
		v1_w = map [count].ret_v1 (); // Get the triangle info for each triangle in the map
		v2_w = map [count].ret_v2 ();
		v3_w = map [count].ret_v3 ();
		col_w = map [count].ret_col ();
		writer.println (v1_w.x); // Write the info
		writer.println (v1_w.y);
		writer.println (v1_w.z);
		writer.println (v2_w.x);
		writer.println (v2_w.y);
		writer.println (v2_w.z);
		writer.println (v3_w.x);
		writer.println (v3_w.y);
		writer.println (v3_w.z);
		writer.println (col_w.getRed ());
		writer.println (col_w.getGreen ());
		writer.println (col_w.getBlue ());
	    }
	    save_file.close ();
	}
	catch (Exception e)  // If something went wrong while saving the file
	{
	    error = true;
	}
    }


    public void load ()  // Load map from the specified file in "filename"
    {
	save_load = 0;
	try // We need to watch for IOExceptions
	{
	    n_map = 0;
	    String eof_check;
	    FileInputStream load_file = new FileInputStream (filename.substring (0, filename.length ()));
	    DataInputStream reader = new DataInputStream (load_file); // We use an input stream so we can read lines easily...
	    vector3d f_vec1, f_vec2, f_vec3;
	    Float f1, f2, f3, r, g, b;
	    for (;;)
	    {
		eof_check = reader.readLine ();
		if (eof_check == null)
		    return;
		f1 = new Float (eof_check); // Float class used for turning strings into floats
		f2 = new Float (reader.readLine ());
		f3 = new Float (reader.readLine ());
		f_vec1 = new vector3d (f1.floatValue (), f2.floatValue (), f3.floatValue ());
		f1 = new Float (reader.readLine ());
		f2 = new Float (reader.readLine ());
		f3 = new Float (reader.readLine ());
		f_vec2 = new vector3d (f1.floatValue (), f2.floatValue (), f3.floatValue ());
		f1 = new Float (reader.readLine ());
		f2 = new Float (reader.readLine ());
		f3 = new Float (reader.readLine ());
		f_vec3 = new vector3d (f1.floatValue (), f2.floatValue (), f3.floatValue ());
		r = new Float (reader.readLine ());
		g = new Float (reader.readLine ());
		b = new Float (reader.readLine ());
		map [n_map] = new triangle (f_vec1, f_vec2, f_vec3, new Color ((int) r.floatValue (), (int) g.floatValue (), (int) b.floatValue ()));
		n_map++;
	    }
	}
	catch (Exception e)  // If something went wrong while loading the file
	{
	    error = true;
	}
    }


    public void make_room4box ()  // Empty room in the map for a new Box!
    {
	for (int count = 0 ; count != 12 ; count++) // Empty room in the map for 12 polygons! (initialize 12 polygons in the map)
	{
	    map [n_map + count] = new triangle (new vector3d (0, 0, 0), new vector3d (0, 0, 0), new vector3d (0, 0, 0), Color.black);
	}


	n_map += 12;
    }


    public void make_room4cylinder ()  // Empty room in the map for a new Box!
    {
	for (int count = 0 ; count != 16 ; count++) // Empty room in the map for 12 polygons! (initialize 12 polygons in the map)
	{
	    map [n_map + count] = new triangle (new vector3d (0, 0, 0), new vector3d (0, 0, 0), new vector3d (0, 0, 0), Color.black);
	}


	n_map += 16;
    }


    public void make_room4prism ()
    {
	for (int count = 0 ; count != 6 ; count++) // Empty room in the map for 6 polygons! (initialize 6 polygons in the map)
	{
	    map [n_map + count] = new triangle (new vector3d (0, 0, 0), new vector3d (0, 0, 0), new vector3d (0, 0, 0), Color.black);
	}


	n_map += 6;
    }


    public void making_cylinder (float x1, float y1, float z1, float r, float h, Color col)
    {
	vector3d draw1, draw2, draw3;
	float rot_deg = 0.0f;
	if (window == 3)
	{
	    for (int count = 8 ; count != 0 ; count--)
	    {
		vector3d rot1 = new vector3d (0, 0, r), rot2 = new vector3d (0, 0, r);
		rot1.RotateY (rot_deg);
		rot2.RotateY (rot_deg + (Math.PI / 4.0f));
		draw1 = new vector3d (rot1.x + x1, y1 + h / 2, rot1.z + z1);
		draw2 = new vector3d (rot2.x + x1, y1 + h / 2, rot2.z + z1);
		draw3 = new vector3d (rot1.x + x1, y1 - h / 2, rot1.z + z1);
		map [n_map - (count * 2)] = new triangle (draw1, draw2, draw3, col);
		draw1 = new vector3d (rot1.x + x1, y1 - h / 2, rot1.z + z1);
		draw2 = new vector3d (rot2.x + x1, y1 - h / 2, rot2.z + z1);
		draw3 = new vector3d (rot2.x + x1, y1 + h / 2, rot2.z + z1);
		map [n_map - (count * 2) + 1] = new triangle (draw1, draw2, draw3, col);
		rot_deg += (Math.PI / 4.0f);
	    }
	}


	if (window == 2)
	{
	    for (int count = 8 ; count != 0 ; count--)
	    {
		vector3d rot1 = new vector3d (r, 0, 0), rot2 = new vector3d (r, 0, 0);
		rot1.RotateZ (rot_deg);
		rot2.RotateZ (rot_deg + (Math.PI / 4.0f));
		draw1 = new vector3d (x1 + rot1.x, y1 + rot1.y, z1 + h / 2);
		draw2 = new vector3d (x1 + rot2.x, y1 + rot2.y, z1 + h / 2);
		draw3 = new vector3d (x1 + rot1.x, y1 + rot1.y, z1 - h / 2);
		map [n_map - (count * 2)] = new triangle (draw1, draw2, draw3, col);
		draw1 = new vector3d (x1 + rot2.x, y1 + rot2.y, z1 + h / 2);
		draw2 = new vector3d (x1 + rot2.x, y1 + rot2.y, z1 - h / 2);
		draw3 = new vector3d (x1 + rot1.x, y1 + rot1.y, z1 - h / 2);
		map [n_map - (count * 2) + 1] = new triangle (draw1, draw2, draw3, col);
		rot_deg += (Math.PI / 4.0f);
	    }
	}


	if (window == 1)
	{
	    for (int count = 8 ; count != 0 ; count--)
	    {
		vector3d rot1 = new vector3d (0, 0, r), rot2 = new vector3d (0, 0, r);
		rot1.RotateX (rot_deg);
		rot2.RotateX (rot_deg + (Math.PI / 4.0f));
		draw1 = new vector3d (x1 + h / 2, y1 + rot1.y, z1 + rot1.z);
		draw2 = new vector3d (x1 + h / 2, y1 + rot2.y, z1 + rot2.z);
		draw3 = new vector3d (x1 - h / 2, y1 + rot1.y, z1 + rot1.z);
		map [n_map - (count * 2)] = new triangle (draw1, draw2, draw3, col);
		draw1 = new vector3d (x1 + h / 2, y1 + rot2.y, z1 + rot2.z);
		draw2 = new vector3d (x1 - h / 2, y1 + rot2.y, z1 + rot2.z);
		draw3 = new vector3d (x1 - h / 2, y1 + rot1.y, z1 + rot1.z);
		map [n_map - (count * 2) + 1] = new triangle (draw1, draw2, draw3, col);
		rot_deg += (Math.PI / 4.0f);
	    }
	}
    }


    public void making_prism (float x1, float y1, float z1, float x2, float y2, float z2, float xt, float yt, float zt, Color col)  // Make a prism with the specifications given in the next empty place in the map
    {
	vector3d draw1, draw2, draw3;
	if (window != 1)
	{
	    draw1 = new vector3d (x1, y1, z1); // Base
	    draw2 = new vector3d (x2, y1, z2);
	    draw3 = new vector3d (x1, y2, z1);

	    map [n_map - 6] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x2, y1, z2);
	    draw2 = new vector3d (x2, y2, z2);
	    draw3 = new vector3d (x1, y2, z1);

	    map [n_map - 5] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x1, y1, z1); // Top
	    draw2 = new vector3d (xt, yt, zt);
	    draw3 = new vector3d (x2, y1, z2);

	    map [n_map - 4] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x2, y1, z2);
	    draw2 = new vector3d (xt, yt, zt);
	    draw3 = new vector3d (x2, y2, z2);

	    map [n_map - 3] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x2, y2, z2);
	    draw2 = new vector3d (xt, yt, zt);
	    draw3 = new vector3d (x1, y2, z1);

	    map [n_map - 2] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x1, y2, z1);
	    draw2 = new vector3d (xt, yt, zt);
	    draw3 = new vector3d (x1, y1, z1);

	    map [n_map - 1] = new triangle (draw1, draw2, draw3, col);
	}


	else
	{
	    draw1 = new vector3d (x1, y1, z1); // Base
	    draw2 = new vector3d (x1, y1, z2);
	    draw3 = new vector3d (x2, y1, z2);

	    map [n_map - 6] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x1, y1, z1);
	    draw2 = new vector3d (x2, y1, z1);
	    draw3 = new vector3d (x2, y1, z2);

	    map [n_map - 5] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x1, y1, z1); // Top
	    draw2 = new vector3d (xt, yt, zt);
	    draw3 = new vector3d (x1, y1, z2);

	    map [n_map - 4] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x1, y1, z2);
	    draw2 = new vector3d (xt, yt, zt);
	    draw3 = new vector3d (x2, y1, z2);

	    map [n_map - 3] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x2, y2, z2);
	    draw2 = new vector3d (xt, yt, zt);
	    draw3 = new vector3d (x2, y1, z1);

	    map [n_map - 2] = new triangle (draw1, draw2, draw3, col);

	    draw1 = new vector3d (x2, y2, z1);
	    draw2 = new vector3d (xt, yt, zt);
	    draw3 = new vector3d (x1, y1, z1);

	    map [n_map - 1] = new triangle (draw1, draw2, draw3, col);
	}
    }


    public void making_box (float x1, float y1, float z1, float x2, float y2, float z2, Color col)  // Make a box with the specifications given in the next empty place in the map
    {

	vector3d draw1, draw2, draw3;
	draw1 = new vector3d (x1, y1, z1); // Top
	draw2 = new vector3d (x1, y1, z2);
	draw3 = new vector3d (x2, y1, z2);

	map [n_map - 12] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x1, y1, z1);
	draw2 = new vector3d (x2, y1, z1);
	draw3 = new vector3d (x2, y1, z2);

	map [n_map - 11] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x1, y2, z1); // Bottom
	draw2 = new vector3d (x1, y2, z2);
	draw3 = new vector3d (x2, y2, z2);

	map [n_map - 10] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x1, y2, z1);
	draw2 = new vector3d (x2, y2, z1);
	draw3 = new vector3d (x2, y2, z2);

	map [n_map - 9] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x1, y1, z1); // Left
	draw2 = new vector3d (x1, y1, z2);
	draw3 = new vector3d (x1, y2, z2);

	map [n_map - 8] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x1, y1, z1);
	draw2 = new vector3d (x1, y2, z1);
	draw3 = new vector3d (x1, y2, z2);

	map [n_map - 7] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x2, y1, z1); // Right
	draw2 = new vector3d (x2, y1, z2);
	draw3 = new vector3d (x2, y2, z2);

	map [n_map - 6] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x2, y1, z1);
	draw2 = new vector3d (x2, y2, z1);
	draw3 = new vector3d (x2, y2, z2);

	map [n_map - 5] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x1, y1, z2); // Front
	draw2 = new vector3d (x2, y1, z2);
	draw3 = new vector3d (x1, y2, z2);

	map [n_map - 4] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x2, y1, z2);
	draw2 = new vector3d (x2, y2, z2);
	draw3 = new vector3d (x1, y2, z2);

	map [n_map - 3] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x1, y1, z1); // Back
	draw2 = new vector3d (x2, y1, z1);
	draw3 = new vector3d (x2, y2, z1);

	map [n_map - 2] = new triangle (draw1, draw2, draw3, col);

	draw1 = new vector3d (x1, y1, z1);
	draw2 = new vector3d (x1, y2, z1);
	draw3 = new vector3d (x2, y2, z1);

	map [n_map - 1] = new triangle (draw1, draw2, draw3, col);
    }


    public void init ()
    {
	_3d_img = createImage (getSize ().width / 2, getSize ().height / 2); // Initialize the image for each window
	_left_img = createImage (getSize ().width / 2, getSize ().height / 2);
	_top_img = createImage (getSize ().width / 2, getSize ().height / 2);
	_front_img = createImage (getSize ().width / 2, getSize ().height / 2);
	_3d_gr = _3d_img.getGraphics (); // Get the graphics handler for each window!
	_left_gr = _left_img.getGraphics ();
	_top_gr = _top_img.getGraphics ();
	_front_gr = _front_img.getGraphics ();

	width = _3d_img.getWidth (this); // Get the width and height for each window
	height = _3d_img.getHeight (this);

	This_zbuff = new zbuffer (height, width); // Initialize the ZBuffer for the 3d view window
    }


    public boolean mouseUp (Event e, int x, int y)
    {
	this.first_time = true; // User stoped dragging!
	window = 0; // No window selected...cause the mouse button is released!
	return true;
    }


    public boolean mouseDrag (Event e, int x, int y)
    {
	if (this.first_time == true)
	{
	    mousex1 = x;
	    mousey1 = y;
	    if (x > width && y > height)
	    {
		window = 4; // Dragging is going to be done in the 3d projection window
	    }
	    if (x < width && y > height)
	    {
		if (type == 1)
		    make_room4box ();
		if (type == 2)
		    make_room4prism ();
		if (type == 3)
		    make_room4cylinder ();
		current_color = new Color ((int) (Math.random () * 255), (int) (Math.random () * 255), (int) (Math.random () * 255));
		window = 3; // Dragging is going to be done in the front view window
	    }
	    if (x < width && y < height)
	    {
		if (type == 1)
		    make_room4box ();
		if (type == 2)
		    make_room4prism ();
		if (type == 3)
		    make_room4cylinder ();
		current_color = new Color ((int) (Math.random () * 255), (int) (Math.random () * 255), (int) (Math.random () * 255));
		window = 1; // Dragging is going to be done in the top view window
	    }
	    if (x > width && y < height)
	    {
		if (type == 1)
		    make_room4box ();
		if (type == 2)
		    make_room4prism ();
		if (type == 3)
		    make_room4cylinder ();
		current_color = new Color ((int) (Math.random () * 255), (int) (Math.random () * 255), (int) (Math.random () * 255));
		window = 2; // Dragging is going to be done in the left view window
	    }
	    first_time = false;
	    draw ();
	    return true;
	}


	if (window == 3)
	{
	    mousex2 = x;
	    mousey2 = y;
	    if (type == 1)
		making_box (((width / 2) - mousex1) / 20, (((height) / 2) - (mousey1 - height)) / 20, 0, ((width / 2) - mousex2) / 20, (((height) / 2) - (mousey2 - height)) / 20, depth, current_color);
	    if (type == 2)
	    {
		float x1 = ((width / 2) - mousex1) / 20;
		float x2 = ((width / 2) - mousex2) / 20;
		float y1 = (((height) / 2) - (mousey1 - height)) / 20;
		float y2 = (((height) / 2) - (mousey2 - height)) / 20;
		making_prism (x1, y1, 0, x2, y2, 0, (x1 + x2) / 2, (y1 + y2) / 2, depth, current_color);
	    }
	    if (type == 3)
	    {
		float x1 = ((width / 2) - mousex1) / 20;
		float x2 = ((width / 2) - mousex2) / 20;
		float y1 = (((height) / 2) - (mousey1 - height)) / 20;
		float y2 = (((height) / 2) - (mousey2 - height)) / 20;
		making_cylinder ((x1 + x2) / 2, (y1 + y2) / 2, 0, Math.abs ((x2 - x1) / 2), Math.abs (y2 - y1), current_color);
	    }
	    draw ();
	    return true;
	}


	if (window == 2)
	{
	    mousex2 = x;
	    mousey2 = y;
	    if (type == 1)
		making_box (0, (((height) / 2) - mousey1) / 20, (mousex1 - width - (width / 2)) / 20, depth, (((height) / 2) - mousey2) / 20, (mousex2 - width - (width / 2)) / 20, current_color);
	    if (type == 2)
	    {
		float z1 = (mousex1 - width - (width / 2)) / 20;
		float z2 = (mousex2 - width - (width / 2)) / 20;
		float y1 = (((height) / 2) - mousey1) / 20;
		float y2 = (((height) / 2) - mousey2) / 20;
		making_prism (0, y1, z1, 0, y2, z2, depth, (y1 + y2) / 2, (z1 + z2) / 2, current_color);
	    }
	    if (type == 3)
	    {
		float z1 = (mousex1 - width - (width / 2)) / 20;
		float z2 = (mousex2 - width - (width / 2)) / 20;
		float y1 = (((height) / 2) - mousey1) / 20;
		float y2 = (((height) / 2) - mousey2) / 20;
		making_cylinder (0, (y1 + y2) / 2, (z1 + z2) / 2, Math.abs ((y2 - y1) / 2), Math.abs ((z2 - z1)), current_color);
	    }
	    draw ();
	    return true;
	}


	if (window == 1)
	{
	    mousex2 = x;
	    mousey2 = y;
	    if (type == 1)
		making_box (((width / 2) - mousex1) / 20, 0, (mousey1 - ((height) / 2)) / 20, ((width / 2) - mousex2) / 20, depth, (mousey2 - ((height) / 2)) / 20, current_color);
	    if (type == 2)
	    {
		float x1 = ((width / 2) - mousex1) / 20;
		float x2 = ((width / 2) - mousex2) / 20;
		float z1 = (mousey1 - ((height) / 2)) / 20;
		float z2 = (mousey2 - ((height) / 2)) / 20;
		making_prism (x1, 0, z1, x2, 0, z2, (x1 + x2) / 2, depth, (z1 + z2) / 2, current_color);
	    }
	    if (type == 3)
	    {
		float x1 = ((width / 2) - mousex1) / 20;
		float x2 = ((width / 2) - mousex2) / 20;
		float z1 = (mousey1 - ((height) / 2)) / 20;
		float z2 = (mousey2 - ((height) / 2)) / 20;
		making_cylinder ((x1 + x2) / 2, 0, (z1 + z2) / 2, Math.abs ((z2 - z1) / 2), Math.abs (x2 - x1), current_color);
	    }
	    draw ();
	    return true;
	}


	if (window == 4) // Drag in the 3d projection window (Rotate head in world)
	{
	    mousex2 = x;
	    mousey2 = y;

	    lookdeg_y += (mousex2 - mousex1) / 120; // This is actually done so that the magnitude of lookdeg_y doesn't get too big!
	    if (lookdeg_y < 0.0f)
		lookdeg_y = (float) (2 * Math.PI);
	    else if (lookdeg_y > 2 * Math.PI)
		lookdeg_y = 0.0f;

	    lookdeg_x += (mousey2 - mousey1) / 120; // This is done so that the viewer doesn't look upside down,...,just like a human head!
	    if (lookdeg_x < -0.5f * Math.PI + 0.1f)
		lookdeg_x = (float) (- 0.5f * Math.PI + 0.1f);
	    else if (lookdeg_x > 0.5f * Math.PI - 0.1f)
		lookdeg_x = (float) (0.5f * Math.PI - 0.1f);

	    mousex1 = mousex2;
	    mousey1 = mousey2;

	    eye = new vector3d (0, 0, dist);
	    eye.RotateX (lookdeg_x);
	    eye.RotateY (lookdeg_y);

	    draw ();
	    return true;
	}


	return true;
    }


    public boolean keyDown (Event e, int c)
    {
	if (window == 1 || window == 2 || window == 3) // If we're dragging and pressing a key in one of the 2d windows!
	{
	    if (c == 'w')
		depth += 0.05f;
	    if (c == 's')
		depth -= 0.05f;

	    if (window == 3) // Make the new box with the new depth!
	    {
		if (type == 1)
		    making_box (((width / 2) - mousex1) / 20, (((height) / 2) - (mousey1 - height)) / 20, 0, ((width / 2) - mousex2) / 20, (((height) / 2) - (mousey2 - height)) / 20, depth, current_color);
		if (type == 2)
		{
		    float x1 = ((width / 2) - mousex1) / 20;
		    float x2 = ((width / 2) - mousex2) / 20;
		    float y1 = (((height) / 2) - (mousey1 - height)) / 20;
		    float y2 = (((height) / 2) - (mousey2 - height)) / 20;
		    making_prism (x1, y1, 0, x2, y2, 0, (x1 + x2) / 2, (y1 + y2) / 2, depth, current_color);
		}
		draw ();
		return true;
	    }
	    if (window == 2) // Make the new box with the new depth!
	    {
		if (type == 1)
		    making_box (0, (((height) / 2) - mousey1) / 20, (mousex1 - width - (width / 2)) / 20, depth, (((height) / 2) - mousey2) / 20, (mousex2 - width - (width / 2)) / 20, current_color);
		if (type == 2)
		{
		    float z1 = (mousex1 - width - (width / 2)) / 20;
		    float z2 = (mousex2 - width - (width / 2)) / 20;
		    float y1 = (((height) / 2) - mousey1) / 20;
		    float y2 = (((height) / 2) - mousey2) / 20;
		    making_prism (0, y1, z1, 0, y2, z2, depth, (y1 + y2) / 2, (z1 + z2) / 2, current_color);
		}
		draw ();
		return true;
	    }
	    if (window == 1) // Make the new box with the new depth!
	    {
		if (type == 1)
		    making_box (((width / 2) - mousex1) / 20, 0, (mousey1 - ((height) / 2)) / 20, ((width / 2) - mousex2) / 20, depth, (mousey2 - ((height) / 2)) / 20, current_color);
		if (type == 2)
		{
		    float x1 = ((width / 2) - mousex1) / 20;
		    float x2 = ((width / 2) - mousex2) / 20;
		    float z1 = (mousey1 - ((height) / 2)) / 20;
		    float z2 = (mousey2 - ((height) / 2)) / 20;
		    making_prism (x1, 0, z1, x2, 0, z2, (x1 + x2) / 2, depth, (z1 + z2) / 2, current_color);
		}
		draw ();
		return true;
	    }
	    return true;
	}
	else // We're pressing a key normaly!
	{
	    if (getfile == true) // We want to get an input (Filename in this case!)
	    {
		if (c == '\n')
		{
		    getfile = false;
		    if (save_load == 1)
			load ();
		    if (save_load == 2)
			save ();
		    draw ();
		    return true;
		}
		if (c != '-')
		{
		    if (filename.length () <= 15) // Maximum chars = 15
			filename = new String (filename + (char) (c)); // Add a char to the string
		}
		else if (filename.length () - 1 >= 0) // Minimum chars = 0 (duh...)
		{
		    filename = filename.substring (0, filename.length () - 1); // Decrease the length of the string by 1
		}
		draw ();
		return true;
	    }
	    if (c == 'l')
	    {
		getfile = true;
		save_load = 1;
		filename = new String ("");
		draw ();
		return true;
	    }
	    if (c == 'p')
	    {
		getfile = true;
		save_load = 2;
		filename = new String ("");
		draw ();
		return true;
	    }
	    if (c == '0')
	    {
		n_map = 0;
		draw ();
		return true;
	    }
	    if (c == '1')
		type = 1;
	    if (c == '2')
		type = 2;
	    if (c == '3')
		type = 3;

	    if (c == 'w')
	    {
		dist -= 0.5f;
		if (dist < 2.0f)
		    dist = 2.0f;
	    }
	    if (c == 's')
	    {
		dist += 0.5f;
	    }

	    eye = new vector3d (0, 0, dist);
	    eye.RotateX (lookdeg_x);
	    eye.RotateY (lookdeg_y);

	    draw ();
	    return true;
	}
    }


    public void draw ()
    {
	This_zbuff.clear (); // Clear the ZBuffer for the 3d view window

	applet_gr = this.getGraphics (); // Get Graphics handler for the Applet itself!

	_3d_gr.setColor (Color.black); // Clear the 3d view window
	_3d_gr.fillRect (0, 0, getSize ().width, getSize ().height);
	_3d_gr.setColor (Color.white); // Clear the 3d view window
	_3d_gr.drawString ("3d", 20, 20);

	_left_gr.setColor (Color.lightGray); // Clear the left view window
	_left_gr.fillRect (0, 0, getSize ().width, getSize ().height);
	_left_gr.setColor (Color.white);
	_left_gr.drawLine (0, height / 2, width, height / 2);
	_left_gr.drawLine (width / 2, 0, width / 2, height);
	_left_gr.drawString ("Left", 20, 20);

	_top_gr.setColor (Color.lightGray); // Clear the top view window
	_top_gr.fillRect (0, 0, getSize ().width, getSize ().height);
	_top_gr.setColor (Color.white);
	_top_gr.drawLine (0, height / 2, width, height / 2);
	_top_gr.drawLine (width / 2, 0, width / 2, height);
	_top_gr.drawString ("Top", 20, 20);

	_front_gr.setColor (Color.lightGray); // Clear the front view window
	_front_gr.fillRect (0, 0, getSize ().width, getSize ().height);
	_front_gr.setColor (Color.white);
	_front_gr.drawLine (0, height / 2, width, height / 2);
	_front_gr.drawLine (width / 2, 0, width / 2, height);
	_front_gr.drawString ("Front", 20, 20);


	for (int count = 0 ; count != n_map ; count++) // Draw the content of the four windows (Draw each polygon in each window in the mode specified)
	{
	    map [count].draw ();
	}

	if (error == true) // Error handling!
	{
	    applet_gr.drawString ("Error,could not work with file!", 100, 100);
	    try
	    {
		Thread.sleep (1000);
	    }
	    catch (Exception e)
	    {
	    }
	    error = false;
	    filename = new String ("");
	}

	if (getfile == true)
	{
	    _top_gr.setColor (Color.gray);
	    _top_gr.fillRect (20, 20, 250, 100);
	    _top_gr.setColor (Color.white);
	    if (save_load == 1)
		_top_gr.drawString ("Enter Filename to load:", 40, 40);
	    if (save_load == 2)
		_top_gr.drawString ("Enter Filename to save to:", 40, 40);
	    _top_gr.setColor (Color.green);
	    _top_gr.drawString (filename, 40, 60);
	}


	applet_gr.setColor (Color.red); // Draw the structure lines of the applet
	applet_gr.drawLine (0, getSize ().height / 2, getSize ().width, getSize ().height / 2);
	applet_gr.drawLine (getSize ().width / 2, 0, getSize ().width / 2, getSize ().height);

	applet_gr.drawImage (_3d_img, (getSize ().width / 2) + 1, (getSize ().height / 2) + 1, this);
	applet_gr.drawImage (_top_img, 0, 0, this);
	applet_gr.drawImage (_left_img, (getSize ().width / 2) + 1, 0, this);
	applet_gr.drawImage (_front_img, 0, (getSize ().height / 2) + 1, this);
    }


    public void paint (Graphics g)
    {
	draw ();
    }
}


class zbuffer
{
    public static double buffer [] [];
    int width, height;
    zbuffer (int ymax, int xmax)  // Initialize the ZBuffer
    {
	int x = 0, y = 0;
	buffer = new double [ymax] [xmax];
	this.width = xmax;
	this.height = ymax;
    }


    void clear ()  // Clear the ZBuffer
    {
	int x = 0, y = 0;
	while (y != height)
	{
	    while (x != width)
	    {
		buffer [y] [x] = -200000.0;
		x++;
	    }
	    x = 0;
	    y++;
	}
    }


    void set (int y, int x, double z)  // Set the z of a pixel in the ZBuffer
    {
	int set_x = x, set_y = y; // These are set for "Buffer Overflow!" prevention,...,I hope that doesn't triger anything in some
	if (set_x < 0)            // People ;D (Like,Maybe,Hackers or something,...,heh,nevermind!)
	    set_x = 0;
	if (set_x > width - 1)
	    set_x = width - 1;
	if (set_y < 0)
	    set_y = 0;
	if (set_y > height - 1)
	    set_y = height - 1;
	buffer [set_y] [set_x] = z;
    }


    double check (int y, int x)  // Check a point's z in the ZBuffer
    {
	int check_x = x, check_y = y; // These are set for "Buffer Overflow!" prevention,...,I hope that doesn't triger anything in some
	if (check_x < 0)              // People ;D (Like,Maybe,Hackers or something,...,heh,nevermind!)
	    check_x = 0;
	if (check_x > width - 1)
	    check_x = width - 1;
	if (check_y < 0)
	    check_y = 0;
	if (check_y > height - 1)
	    check_y = height - 1;
	return buffer [check_y] [check_x];
    }
}


class vector3d // vector3d class,...for creating points
{
    float x, y, z, x2d, y2d, xtr, ytr, ztr; // x2d = x in screen,y2d = y in screen,dz = z in screen,xtr = x transformed,ytr = y transformed,ztr = z transformed

    vector3d (float x, float y, float z)  // This type is used for points which will never be drawn
    {
	this.x = x;
	this.y = y;
	this.z = z;
    }


    void set2dpos ()  // Set the 2d position of a vector
    {
	int xmax = FramerCrane.width;
	int ymax = FramerCrane.height;
	float eyex = FramerCrane.eye.x;
	float eyey = FramerCrane.eye.y;
	float eyez = FramerCrane.eye.z;
	float lookx = FramerCrane.look.x;
	float looky = FramerCrane.look.y;
	float lookz = FramerCrane.look.z;

	lookx = lookx - eyex;
	looky = looky - eyey;
	lookz = lookz - eyez;

	float dx = x - eyex;
	float dy = y - eyey;
	float dz = z - eyez;

	float xdeg, ydeg;
	xdeg = Angle_Bet_Vecs (lookx, looky, lookz, lookx, 0, lookz);
	ydeg = Angle_Bet_Vecs (lookx, 0, lookz, 0, 0, - 1);
	if (lookx < 0)
	    ydeg = -ydeg;
	if (looky < 0)
	    xdeg = -xdeg;

	float tmpx = dx, tmpy = dy, tmpz = dz;

	tmpx = dx;
	tmpz = dz;
	dx = (float) (tmpx * Math.cos (- ydeg) - tmpz * Math.sin (- ydeg));
	dz = (float) (tmpz * Math.cos (- ydeg) + tmpx * Math.sin (- ydeg));

	tmpy = dy;
	tmpz = dz;
	dy = (float) (tmpy * Math.cos (- xdeg) - tmpz * Math.sin (- xdeg));
	dz = (float) (tmpz * Math.cos (- xdeg) + tmpy * Math.sin (- xdeg));

	if (dz == 0)
	    dz = 1;

	xtr = dx;
	ytr = dy;
	ztr = dz;

	if (dz < 0)
	{
	    x2d = 450 * dx / dz + (xmax / 2);
	    y2d = 450 * dy / dz + (ymax / 2);
	}
    }


    float Angle_Bet_Vecs (float x1, float y1, float z1, float x2, float y2, float z2)  // Find the angle between 2 vectors
    {
	float vec1_size = (float) (Math.sqrt (Math.pow (x1, 2) + Math.pow (y1, 2) + Math.pow (z1, 2)));
	float vec2_size = (float) (Math.sqrt (Math.pow (x2, 2) + Math.pow (y2, 2) + Math.pow (z2, 2)));
	float dot = (x1 * x2) + (y1 * y2) + (z1 * z2);
	float cosofa = dot / (vec1_size * vec2_size);

	// There is a bug in java's math functions that's why we do this check!
	if (cosofa < -1)
	    cosofa = -1;
	if (cosofa > 1)
	    cosofa = 1;

	return (float) (Math.acos (cosofa));
    }


    void RotateX (double deg)  // Rotate the point around the x axis
    {
	float tmpy = y, tmpz = z;
	y = (float) (tmpy * Math.cos (deg) - tmpz * Math.sin (deg));
	z = (float) (tmpz * Math.cos (deg) + tmpy * Math.sin (deg));
    }


    void RotateY (double deg)  // Rotate the point around the y axis
    {
	float tmpx = x, tmpz = z;
	x = (float) (tmpx * Math.cos (deg) - tmpz * Math.sin (deg));
	z = (float) (tmpz * Math.cos (deg) + tmpx * Math.sin (deg));
    }


    void RotateZ (double deg)  // Rotate the point around the z axis
    {
	float tmpx = x, tmpy = y;
	x = (float) (tmpx * Math.cos (deg) - tmpy * Math.sin (deg));
	y = (float) (tmpy * Math.cos (deg) + tmpx * Math.sin (deg));
    }
}


class plane // plane class,...for planes!
{
    float a, b, c, d;
    plane (float a, float b, float c, float d)
    {
	this.a = a;
	this.b = b;
	this.c = c;
	this.d = d;
    }
}


class triangle // triangle class,...for creating a triangle
{
    vector3d v1, v2, v3;
    Color col;
    plane pl_tr; // The plane of the polygon when completely transformed and rotated according to the look and eye points (used for ZBuffer)
    triangle (vector3d v1, vector3d v2, vector3d v3, Color col)
    {
	this.v1 = v1;
	this.v2 = v2;
	this.v3 = v3;
	this.col = col;
    }


    vector3d ret_v1 ()
    {
	vector3d ret;
	ret = new vector3d (v1.x, v1.y, v1.z);
	return v1;
    }


    vector3d ret_v2 ()
    {
	vector3d ret;
	ret = new vector3d (v2.x, v2.y, v2.z);
	return v2;
    }


    vector3d ret_v3 ()
    {
	vector3d ret;
	ret = new vector3d (v3.x, v3.y, v3.z);
	return v3;
    }


    Color ret_col ()
    {
	Color ret;
	ret = new Color (col.getRed (), col.getGreen (), col.getBlue ());
	return col;
    }


    vector3d cross_product (vector3d v1_i, vector3d v2_i)  // Cross product of 2 vectors
    {
	float x, y, z;
	vector3d res;
	x = (v1_i.y * v2_i.z) - (v2_i.y * v1_i.z);
	y = (v1_i.z * v2_i.x) - (v2_i.z * v1_i.x);
	z = (v1_i.x * v2_i.y) - (v2_i.x * v1_i.y);
	res = new vector3d (x, y, z);
	return res;
    }


    float dot_product (vector3d v1_i, vector3d v2_i)  // Dot product of 2 vectors
    {
	return (float) (v1_i.x * v2_i.x + v1_i.y * v2_i.y + v1_i.z * v2_i.z);
    }


    void draw ()
    {
	v1.set2dpos (); // Set the 2d position of the vertices of the edges of the triangle
	v2.set2dpos ();
	v3.set2dpos ();

	vector3d normal, edge1, edge2; // Find the plane of the eye/look transformed and rotated triangle
	float d;
	edge1 = new vector3d (v1.xtr - v2.xtr, v1.ytr - v2.ytr, v1.ztr - v2.ztr);
	edge2 = new vector3d (v3.xtr - v2.xtr, v3.ytr - v2.ytr, v3.ztr - v2.ztr);
	normal = cross_product (edge1, edge2);
	vector3d apoint = new vector3d (v1.xtr, v1.ytr, v1.ztr);
	d = -1.0f * dot_product (apoint, normal);
	this.pl_tr = new plane (normal.x, normal.y, normal.z, d);

	FramerCrane._top_gr.setColor (Color.black); // Draw the polygon in the top view
	FramerCrane._top_gr.drawLine ((int) ((FramerCrane.width / 2) - v1.x * 20), (int) ((FramerCrane.height / 2) + v1.z * 20), (int) ((FramerCrane.width / 2) - v2.x * 20), (int) ((FramerCrane.height / 2) + v2.z * 20));
	FramerCrane._top_gr.drawLine ((int) ((FramerCrane.width / 2) - v2.x * 20), (int) ((FramerCrane.height / 2) + v2.z * 20), (int) ((FramerCrane.width / 2) - v3.x * 20), (int) ((FramerCrane.height / 2) + v3.z * 20));
	FramerCrane._top_gr.drawLine ((int) ((FramerCrane.width / 2) - v1.x * 20), (int) ((FramerCrane.height / 2) + v1.z * 20), (int) ((FramerCrane.width / 2) - v3.x * 20), (int) ((FramerCrane.height / 2) + v3.z * 20));

	FramerCrane._left_gr.setColor (Color.black); // Draw the polygon in the left view
	FramerCrane._left_gr.drawLine ((int) ((FramerCrane.width / 2) + v1.z * 20), (int) ((FramerCrane.height / 2) - v1.y * 20), (int) ((FramerCrane.width / 2) + v2.z * 20), (int) ((FramerCrane.height / 2) - v2.y * 20));
	FramerCrane._left_gr.drawLine ((int) ((FramerCrane.width / 2) + v2.z * 20), (int) ((FramerCrane.height / 2) - v2.y * 20), (int) ((FramerCrane.width / 2) + v3.z * 20), (int) ((FramerCrane.height / 2) - v3.y * 20));
	FramerCrane._left_gr.drawLine ((int) ((FramerCrane.width / 2) + v1.z * 20), (int) ((FramerCrane.height / 2) - v1.y * 20), (int) ((FramerCrane.width / 2) + v3.z * 20), (int) ((FramerCrane.height / 2) - v3.y * 20));

	FramerCrane._front_gr.setColor (Color.black); // Draw the polygon in the front view
	FramerCrane._front_gr.drawLine ((int) ((FramerCrane.width / 2) - v1.x * 20), (int) ((FramerCrane.height / 2) - v1.y * 20), (int) ((FramerCrane.width / 2) - v2.x * 20), (int) ((FramerCrane.height / 2) - v2.y * 20));
	FramerCrane._front_gr.drawLine ((int) ((FramerCrane.width / 2) - v2.x * 20), (int) ((FramerCrane.height / 2) - v2.y * 20), (int) ((FramerCrane.width / 2) - v3.x * 20), (int) ((FramerCrane.height / 2) - v3.y * 20));
	FramerCrane._front_gr.drawLine ((int) ((FramerCrane.width / 2) - v1.x * 20), (int) ((FramerCrane.height / 2) - v1.y * 20), (int) ((FramerCrane.width / 2) - v3.x * 20), (int) ((FramerCrane.height / 2) - v3.y * 20));

	// Draw the polygon in the 3d projection view
	if (v1.x2d != 0 && v1.y2d != 0 && v2.x2d != 0 && v2.y2d != 0 && v3.x2d != 0 && v3.y2d != 0)
	    draw_single_poly (new Point ((int) v1.x2d, (int) v1.y2d), new Point ((int) v2.x2d, (int) v2.y2d), new Point ((int) v3.x2d, (int) v3.y2d), col);
    }


    void draw_single_poly (Point p1, Point p2, Point p3, Color col)
    {
	int xmax = FramerCrane.width;
	int ymax = FramerCrane.height;

	Point array [] = new Point [3]; // Arrange points in a specific order (needed for drawing the polygon)
	if (p1.y <= p2.y && p2.y <= p3.y)
	{
	    array [0] = p1;
	    array [1] = p2;
	    array [2] = p3;
	}


	if (p1.y <= p3.y && p3.y <= p2.y)
	{
	    array [0] = p1;
	    array [1] = p3;
	    array [2] = p2;
	}


	if (p2.y <= p1.y && p1.y <= p3.y)
	{
	    array [0] = p2;
	    array [1] = p1;
	    array [2] = p3;
	}


	if (p2.y <= p3.y && p3.y <= p1.y)
	{
	    array [0] = p2;
	    array [1] = p3;
	    array [2] = p1;
	}


	if (p3.y <= p1.y && p1.y <= p2.y)
	{
	    array [0] = p3;
	    array [1] = p1;
	    array [2] = p2;
	}


	if (p3.y <= p2.y && p2.y <= p1.y)
	{
	    array [0] = p3;
	    array [1] = p2;
	    array [2] = p1;
	}


	if (array [0].y == array [1].y && array [0].y > 0 && array [0].y < ymax) // If the top half is a line draw the line
	{
	    if (array [0].x > array [1].x)
		hor_line (array [0].y, array [1].x, array [0].x, col);
	    else
		hor_line (array [0].y, array [0].x, array [1].x, col);
	}
	else // If not,then draw the top half triangle
	{
	    for (int nowy = array [0].y ; nowy <= array [1].y ; nowy++)
	    {
		if (nowy > 0 && nowy < ymax)
		{
		    int x1, x2;
		    float percent = (nowy * 1.0f - array [0].y) / (array [1].y - array [0].y);
		    x1 = (int) ((percent * (array [1].x - array [0].x)) + array [0].x);
		    float percent2 = ((nowy * 1.0f - array [0].y) / (array [2].y - array [0].y));
		    x2 = (int) ((percent2 * (array [2].x - array [0].x)) + array [0].x);
		    if (x1 < 0)
			x1 = 0;
		    if (x2 < 0)
			x2 = 0;
		    if (x1 > xmax)
			x1 = xmax;
		    if (x2 > xmax)
			x2 = xmax;
		    if (x2 > x1)
			hor_line (nowy, x1, x2, col);
		    else
			hor_line (nowy, x2, x1, col);
		}
	    }
	}


	if (array [1].y == array [2].y && array [1].y > 0 && array [1].y < ymax) // If the bottom half is a line draw the line
	{
	    if (array [1].x > array [2].x)
		hor_line (array [1].y, array [2].x, array [1].x, col);
	    else
		hor_line (array [1].y, array [1].x, array [2].x, col);
	}
	else // If not,then draw the bottom half triangle
	{
	    for (int nowy = array [1].y ; nowy <= array [2].y ; nowy++)
	    {
		if (nowy > 0 && nowy < ymax)
		{
		    int x1, x2;
		    float percent = (nowy * 1.0f - array [1].y) / (array [2].y - array [1].y);
		    x1 = (int) ((percent * (array [2].x - array [1].x)) + array [1].x);
		    float percent2 = ((nowy * 1.0f - array [0].y) / (array [2].y - array [0].y));
		    x2 = (int) ((percent2 * (array [2].x - array [0].x)) + array [0].x);
		    if (x1 < 0)
			x1 = 1;
		    if (x2 < 0)
			x2 = 1;
		    if (x1 > xmax)
			x1 = xmax;
		    if (x2 > xmax)
			x2 = xmax;
		    if (x2 > x1)
			hor_line (nowy, x1, x2, col);
		    else
			hor_line (nowy, x2, x1, col);
		}
	    }
	}
    }


    void hor_line (int y, int x1, int x2, Color col)  // Draw a horizontal line
    {
	Graphics g = FramerCrane._3d_gr;
	int xmax = FramerCrane.width;
	int ymax = FramerCrane.height;

	g.setColor (col);
	int c = x1;
	while (c < x2)
	{
	    float par1 = (pl_tr.a * (c - xmax / 2)) / 450;
	    float par2 = (pl_tr.b * (y - ymax / 2)) / 450;
	    float z = (- pl_tr.d) / (par1 + par2 + pl_tr.c); // Calculate the "Z" of the "Pixel"
	    if (FramerCrane.This_zbuff.check (y - 1, c - 1) < z && z < 0) // Check in ZBuffer if it's the closest point to viewer in it's screen (x,y) position
	    {
		FramerCrane.This_zbuff.set (y - 1, c - 1, z); // Set the pixel as the closest to the viewer in the screen and ZBuffer!
		g.drawLine (c, y, c, y); // Draw the Pixel!!!
	    }
	    c++;
	}
    }
}


