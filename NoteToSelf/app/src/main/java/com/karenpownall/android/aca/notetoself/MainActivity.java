package com.karenpownall.android.aca.notetoself;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Animation mAnimFlash;
    Animation mFadeIn;
    private NoteAdapter mNoteAdapter;
    private boolean mSound;
    private int mAnimOption;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNoteAdapter = new NoteAdapter();
        ListView listNote = (ListView) findViewById(R.id.listView);
        listNote.setAdapter(mNoteAdapter);

        //Handle clicks on the ListView
        listNote.setOnItemClickListener(new AdapterView.OnItemClickListener(){ //inner annonymous class
            @Override
            public void onItemClick(AdapterView<?>adapter, View view, int whichItem, long id){
                /*
                Create a temporary Note
                which is a reference to the Note
                that has been clicked
                 */
                Note tempNote = mNoteAdapter.getItem(whichItem);

                //Create a new dialog window
                DialogShowNote dialog = new DialogShowNote();
                //Send in a reference to teh note to be shown
                dialog.sendNoteSelected(tempNote);

                //show the dialog window with the note in it
                dialog.show(getFragmentManager(), "");
            }
        });
    } //end of onCreate

    public void createNewNote(Note n) {
        mNoteAdapter.addNote(n);
    }

    @Override
    protected void onResume(){
        super.onResume();

        mPrefs = getSharedPreferences("Note to Self", MODE_PRIVATE);
        mSound = mPrefs.getBoolean("sound", true);
        mAnimOption = mPrefs.getInt("anim option", SettingsActivity.FAST);

        mAnimFlash = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flash);
        mFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        // Set the rate of flash based on settings
        if(mAnimOption == SettingsActivity.FAST){

            mAnimFlash.setDuration(100);
            Log.i("anim = ",""+ mAnimOption);
        }else if(mAnimOption == SettingsActivity.SLOW){

            Log.i("anim = ",""+ mAnimOption);
            mAnimFlash.setDuration(1000);
        }

        mNoteAdapter.notifyDataSetChanged();
    } //end of onResume

    @Override
    protected void onPause(){
        super.onPause();
        mNoteAdapter.saveNotes();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_add) {
            DialogNewNote dialog = new DialogNewNote();
            dialog.show(getFragmentManager(), "456");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //inner class
    public class NoteAdapter extends BaseAdapter{
        private JSONSerializer mSerializer;
        List<Note> noteList = new ArrayList<Note>();

        public NoteAdapter(){
            mSerializer = new JSONSerializer("NoteToSelf.json", MainActivity.this.getApplicationContext());
            try {
                noteList = mSerializer.load();
            } catch (Exception e){
                noteList = new ArrayList<Note>();
                Log.e("Error loading notes: ", "", e);
            }
        }

        @Override
        public int getCount(){
            return noteList.size();
        }
        @Override
        public Note getItem(int whichItem){
            return noteList.get(whichItem);
        }
        @Override
        public long getItemId(int whichItem){
            return whichItem;
        }
        @Override
        public View getView(int whichItem, View view, ViewGroup viewGroup){
            //implement this method next
            //Has view been inflated already
            if (view == null){
                //If not, do so here
                //first create a LayoutInflater
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //Now instantiate view using inflater.inflate
                //using the listitem layout
                view = inflater.inflate(R.layout.listitem, viewGroup, false);
                //the false parameter is necessary, because of the way we want to use listitem
            } //End if

            //grab a reference to all TextView and ImageView widgets
            TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            TextView txtDescription = (TextView) view.findViewById(R.id.txtDescription);
            ImageView ivImportant = (ImageView) view.findViewById(R.id.imageViewImportant);
            ImageView ivTodo = (ImageView) view.findViewById(R.id.imageViewTodo);
            ImageView ivIdea = (ImageView) view.findViewById(R.id.imageViewData);

            //hide image view widges that aren't relevant
            Note tempNote = noteList.get(whichItem);

            // To animate or not to animate
            if (tempNote.isImportant() && mAnimOption != SettingsActivity.NONE ) {
                view.setAnimation(mAnimFlash);

            }else{
                view.setAnimation(mFadeIn);
            }

            if (!tempNote.isImportant()){
                ivImportant.setVisibility(View.GONE);
            }
            if (!tempNote.isTodo()){
                ivTodo.setVisibility(View.GONE);
            }
            if (!tempNote.isIdea()){
                ivIdea.setVisibility(View.GONE);
            }

            //add text to the heading and description
            txtTitle.setText(tempNote.getTitle());
            txtDescription.setText(tempNote.getDescription());
            return view;
        } //end of View getView

        public void addNote(Note n){
            noteList.add(n);
            notifyDataSetChanged();
        } //end of addNote

        public void saveNotes(){
            try{
                mSerializer.save(noteList);
            } catch (Exception e){
                Log.e("Error Saving Notes", "", e);
            }
        } //end of saveNote
    } //end of NoteAdapter inner class

} //end of Main Activity
