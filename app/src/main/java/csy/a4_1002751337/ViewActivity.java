package csy.a4_1002751337;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);




    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private String namename;


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_view, container, false);
            TextView nametext = (TextView) rootView.findViewById(R.id.section_label);
            TextView introtext = (TextView) rootView.findViewById(R.id.introText);
            ImageView imgView = (ImageView) rootView.findViewById(R.id.imageView);
            Button buttonGoogle = (Button) rootView.findViewById(R.id.buttongoogle);
            buttonGoogle.setOnClickListener(new ButtonClickListener());

            Bundle args = getArguments();;
            final int pageno=args.getInt(ARG_SECTION_NUMBER);
            DataBase database=new DataBase(getActivity(),"people.db",null,1);
            SQLiteDatabase db = null;
            db = database.getReadableDatabase();
            /*
            Cursor cs=db.rawQuery("SELECT * FROM person", null);
            cs.moveToFirst();
            int i=1;
            while (i<pageno){
                i++;
                cs.moveToNext();
            }
            nametext.setText(cs.getString(cs.getColumnIndex("name")));
            Bitmap bm = BitmapFactory.decodeFile("/mnt/sdcard/csyA4/"+cs.getString(cs.getColumnIndex("img")));
            imgView.setImageBitmap(bm);
            */

            Cursor cs=db.rawQuery("SELECT * FROM person", null);
            if (cs.moveToFirst()){
                int i=1;
                while (i<pageno){
                    i++;
                    cs.moveToNext();
                }
                Bitmap bm = BitmapFactory.decodeFile("/mnt/sdcard/csyA4/"+cs.getString(cs.getColumnIndex("img")));
                imgView.setImageBitmap(bm);
                namename=cs.getString(cs.getColumnIndex("name"));
                nametext.setText(namename);
                introtext.setText(cs.getString(cs.getColumnIndex("introduction")));
                final int _id=cs.getInt(cs.getColumnIndex("_id"));
                cs.close();
                db.close();

                Button buttonDelete = (Button) rootView.findViewById(R.id.buttondelete);
                buttonDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        DataBase database = new DataBase(getActivity(), "people.db", null, 1);
                        SQLiteDatabase db = null;
                        db = database.getReadableDatabase();
                        db.delete("person", "_id=?", new String[]{String.valueOf(_id)});
                        db.close();
                        container.removeView(rootView);
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        mViewPager.setAdapter(mSectionsPagerAdapter);
                    }
                });

            }


            return rootView;
        }

        class ButtonClickListener implements View.OnClickListener{
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.buttongoogle){
                    Intent intent= new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    String link="http://www.google.ca/search?q="+namename;
                    Uri contenturl = Uri.parse(link);
                    intent.setData(contenturl);
                    startActivity(intent);
                }


            }

        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            DataBase database = new DataBase(ViewActivity.this, "people.db", null, 1);
            SQLiteDatabase db = null;
            db = database.getReadableDatabase();
            Cursor cursor=db.rawQuery("SELECT name FROM person", null);
            int num=cursor.getCount();
            cursor.close();
            db.close();
            return num;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            /*
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            */
            return null;
        }
    }
}
