package it.neokree.materialnavigationdrawer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



import java.util.LinkedList;
import java.util.List;

/**
 * Activity that implements ActionBarActivity with a Navigation Drawer with Material Design style
 *
 * @author created by neokree
 */
@SuppressLint("InflateParams")
public abstract class MaterialNavigationDrawer<Fragment> extends ActionBarActivity implements MaterialSectionListener {

    public static final int BOTTOM_SECTION_START = 100;
    public static final int USER_CHANGE_TRANSITION = 500;

    private DrawerLayout layout;
    private ActionBar actionBar;
    private ActionBarDrawerToggle pulsante;
    private ImageView statusBar;
    private Toolbar toolbar;
    private RelativeLayout drawer;
    private ImageView userphoto;
    private ImageView userSecondPhoto;
    private ImageView userThirdPhoto;
    private ImageView usercover;
    private ImageView usercoverSwitcher;
    private TextView username;
    private TextView usermail;
    private LinearLayout sections;
    private LinearLayout bottomSections;

    private List<MaterialSection> sectionList;
    private List<MaterialSection> bottomSectionList;
    private List<MaterialAccount> accountManager;
    private MaterialSection currentSection;
    private MaterialAccount currentAccount;

    private CharSequence title;
    private float density;
    private int primaryColor;

    private View.OnClickListener currentAccountListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // enter into account properties

            if(accountListener != null) {
                accountListener.onAccountOpening(currentAccount);
            }

            // close drawer
            layout.closeDrawer(drawer);

        }
    };
    private View.OnClickListener secondAccountListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // account change
            MaterialAccount account = findAccountNumber(MaterialAccount.SECOND_ACCOUNT);
            if(account != null) {
                if (accountListener != null)
                    accountListener.onChangeAccount(account);

                switchAccounts(account);
            }
            else {// if there is no second account user clicked for open it
                accountListener.onAccountOpening(currentAccount);
                layout.closeDrawer(drawer);
            }

        }
    };
    private View.OnClickListener thirdAccountListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // account change
            MaterialAccount account = findAccountNumber(MaterialAccount.THIRD_ACCOUNT);
            if(account != null) {
                if (accountListener != null)
                    accountListener.onChangeAccount(account);

                switchAccounts(account);
            }
            else {// if there is no second account user clicked for open it
                accountListener.onAccountOpening(currentAccount);
                layout.closeDrawer(drawer);
            }

        }
    };
    private MaterialAccountListener accountListener;

    @Override
    /**
     * Do not Override this method!!! <br>
     * Use init() instead
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_navigation_drawer);
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // init toolbar & status bar
        statusBar = (ImageView) findViewById(R.id.statusBar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // init drawer components
        drawer = (RelativeLayout) this.findViewById(R.id.drawer);
        username = (TextView) this.findViewById(R.id.user_nome);
        usermail = (TextView) this.findViewById(R.id.user_email);
        userphoto = (ImageView) this.findViewById(R.id.user_photo);
        userSecondPhoto = (ImageView) this.findViewById(R.id.user_photo_2);
        userThirdPhoto = (ImageView) this.findViewById(R.id.user_photo_3);
        usercover = (ImageView) this.findViewById(R.id.user_cover);
        usercoverSwitcher = (ImageView) this.findViewById(R.id.user_cover_switcher);
        sections = (LinearLayout) this.findViewById(R.id.sections);
        bottomSections = (LinearLayout) this.findViewById(R.id.bottom_sections);

        // init lists
        sectionList = new LinkedList<>();
        bottomSectionList = new LinkedList<>();
        accountManager = new LinkedList<>();

        // init listeners
        userphoto.setOnClickListener(currentAccountListener);
        usercover.setOnClickListener(currentAccountListener);
        userSecondPhoto.setOnClickListener(secondAccountListener);
        userThirdPhoto.setOnClickListener(thirdAccountListener);

        //get density
        density = this.getResources().getDisplayMetrics().density;

        // get primary color
        Resources.Theme theme = this.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primaryColor = typedValue.data;
        // set darker status bar if device is kitkat
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
            this.statusBar.setImageDrawable(new ColorDrawable(darkenColor(primaryColor)));

        // INIT ACTION BAR
        this.setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        init(savedInstanceState);

        if(sectionList.size() == 0) {
            throw new RuntimeException("You must add at least one Section to top list.");
        }

        // Si preleva il titolo dell'activity
        title = sectionList.get(0).getTitle();

        // si collega il DrawerLayout al codice e gli si setta l'ombra all'apertura
        layout = (DrawerLayout) this.findViewById(R.id.drawer_layout);

        pulsante = new ActionBarDrawerToggle(this,layout,toolbar,R.string.nothing,R.string.nothing) {

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // termina il comando
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // termina il comando
            }

        };

        layout.setDrawerListener(pulsante);

        // si attacca alla usercover un listener
        ViewTreeObserver vto = usercover.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // quando l'immagine e' stata caricata

                // change user space to 16:9
                int width = drawer.getWidth();

                int height = (9 * width) / 16;

                // si toglie l'altezza in eccesso nelle versioni precenti a kitkat
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    height -= (density * 25);
                }

                // set user space
                usercover.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,height));
                usercoverSwitcher.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,height));

                ViewTreeObserver obs = usercover.getViewTreeObserver();
                // si rimuove il listener
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

        // init account views
        if(accountManager.size() > 0) {
            currentAccount = accountManager.get(0);
            notifyAccountDataChanged();
        }

        // init section
        MaterialSection section = sectionList.get(0);
        currentSection = section;
        section.select();
        setFragment((Fragment) section.getTargetFragment(),section.getTitle(),null);
    }

    // Gestione dei Menu -----------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return super.onCreateOptionsMenu(menu);
    }

    /* Chiamata dopo l'invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Se dal drawer si seleziona un oggetto
        if (pulsante.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        pulsante.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {// al cambio di orientamento dello schermo
        super.onConfigurationChanged(newConfig);

        // Passa tutte le configurazioni al drawer
        pulsante.onConfigurationChanged(newConfig);

    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        this.getSupportActionBar().setTitle(title);
    }

    private void setFragment(Fragment fragment,String title,Fragment oldFragment) {
        // si setta il titolo
        setTitle(title);

        // change for default Fragment / support Fragment
        if(fragment instanceof android.app.Fragment) {
            if(oldFragment instanceof android.support.v4.app.Fragment)
                throw new RuntimeException("You should use only one type of Fragment");


            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if(oldFragment != null && fragment != oldFragment)
                ft.remove((android.app.Fragment) oldFragment);

            ft.replace(R.id.frame_container, (android.app.Fragment) fragment).commit();
        }
        else if(fragment instanceof android.support.v4.app.Fragment) {
            if(oldFragment instanceof android.app.Fragment)
                throw new RuntimeException("You should use only one type of Fragment");

            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if(oldFragment != null && oldFragment != fragment)
                ft.remove((android.support.v4.app.Fragment) oldFragment);

            ft.replace(R.id.frame_container, (android.support.v4.app.Fragment) fragment).commit();
        }
        else
            throw new RuntimeException("Fragment must be android.app.Fragment or android.support.v4.app.Fragment");
        
        // si chiude il drawer
        layout.closeDrawer(drawer);
    }

    private MaterialAccount findAccountNumber(int number) {
        for(MaterialAccount account : accountManager)
            if(account.getAccountNumber() == number)
                return account;


        return null;
    }

    private void switchAccounts(final MaterialAccount newAccount) {

        final ImageView floatingImage = new ImageView(this);

        // si calcolano i rettangoli di inizio e fine
        Rect startingRect = new Rect();
        Rect finalRect = new Rect();
        Point offsetHover = new Point();

        // 64dp primary user image / 40dp other user image = 1.6 scale
        float finalScale = 1.6f;

        final int statusBarHeight;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            statusBarHeight = (int) (25 * density);
        }
        else {
            statusBarHeight = 0;
        }

        // si tiene traccia della foto cliccata
        ImageView photoClicked;
        if(newAccount.getAccountNumber() == MaterialAccount.SECOND_ACCOUNT) {
            photoClicked = userSecondPhoto;
        }
        else {
            photoClicked = userThirdPhoto;
        }
        photoClicked.getGlobalVisibleRect(startingRect,offsetHover);
        floatingImage.setImageDrawable(photoClicked.getDrawable());

        // si aggiunge una view nell'esatta posizione dell'altra
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(photoClicked.getWidth(),photoClicked.getHeight());
        params.setMargins(offsetHover.x,offsetHover.y - statusBarHeight,0,0);
        drawer.addView(floatingImage,params);

        // si setta la nuova foto di profilo sopra alla vecchia
        photoClicked.setImageBitmap(currentAccount.getCircularPhoto());

        // si setta la nuova immagine di background da visualizzare sotto la vecchia
        usercoverSwitcher.setImageBitmap(newAccount.getBackground());

        userphoto.getGlobalVisibleRect(finalRect);

        // Si calcola l'offset finale (LARGHEZZA DEL CONTAINER GRANDE - LARGHEZZA DEL CONTAINER PICCOLO / 2) e lo si applica
        int offset = (((finalRect.bottom - finalRect.top) - (startingRect.bottom - finalRect.top)) / 2);
        finalRect.offset(offset,offset - statusBarHeight);
        startingRect.offset(0,-statusBarHeight);

        // si animano le viste
        AnimatorSet set = new AnimatorSet();
       set
               // si ingrandisce l'immagine e la si sposta a sinistra.
               .play(ObjectAnimator.ofFloat(floatingImage, View.X,  startingRect.left,finalRect.left))
               .with(ObjectAnimator.ofFloat(floatingImage, View.Y, startingRect.top, finalRect.top))
               .with(ObjectAnimator.ofFloat(floatingImage, View.SCALE_X, 1f, finalScale))
               .with(ObjectAnimator.ofFloat(floatingImage, View.SCALE_Y, 1f, finalScale))
               .with(ObjectAnimator.ofFloat(userphoto, View.ALPHA,1f,0f))
               .with(ObjectAnimator.ofFloat(usercover, View.ALPHA,1f,0f))
               .with(ObjectAnimator.ofFloat(photoClicked, View.SCALE_X, 0f, 1f))
               .with(ObjectAnimator.ofFloat(photoClicked, View.SCALE_Y, 0f, 1f));
        set.setDuration(USER_CHANGE_TRANSITION);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                // si carica la nuova immagine
                ((View)userphoto).setAlpha(1);
                setFirstAccountPhoto(newAccount.getCircularPhoto());

                // si cancella l'imageview per l'effetto
                drawer.removeView(floatingImage);

                // si cambiano i dati utente
                setUserEmail(newAccount.getSubTitle());
                setUsername(newAccount.getTitle());

                // si cambia l'immagine soprastante
                setDrawerBackground(newAccount.getBackground());
                // si fa tornare il contenuto della cover visibile (ma l'utente non nota nulla)
                ((View)usercover).setAlpha(1);

                // switch numbers
                currentAccount.setAccountNumber(newAccount.getAccountNumber());
                newAccount.setAccountNumber(MaterialAccount.FIRST_ACCOUNT);

                // change pointer to newAccount
                currentAccount = newAccount;

                // si chiude il drawer
                layout.closeDrawer(drawer);

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // se si annulla l'animazione si conclude e basta.
                onAnimationEnd(animation);
            }
        });

        set.start();

    }

    private void setUserEmail(String email) {
        this.usermail.setText(email);
    }

    private void setUsername(String username) {
        this.username.setText(username);
    }

    private void setFirstAccountPhoto(Bitmap photo) {
        userphoto.setImageBitmap(photo);
    }

    private void setSecondAccountPhoto(Bitmap photo) {
        userSecondPhoto.setImageBitmap(photo);
    }

    private void setThirdAccountPhoto(Bitmap photo) {
        userThirdPhoto.setImageBitmap(photo);
    }

    private void setDrawerBackground(Bitmap background) {
        usercover.setImageBitmap(background);
    }

    protected int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    @Override
    public void onClick(MaterialSection section) {

        if(section.getTarget() == MaterialSection.TARGET_FRAGMENT)
        {
            setFragment((Fragment)section.getTargetFragment(),section.getTitle(),(Fragment) currentSection.getTargetFragment());

            // setting toolbar color if is setted
            if(section.hasSectionColor()) {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
                    this.statusBar.setImageDrawable(new ColorDrawable(darkenColor(section.getSectionColor())));
                else
                    this.statusBar.setImageDrawable(new ColorDrawable(section.getSectionColor()));
                this.getToolbar().setBackgroundColor(section.getSectionColor());
            }
            else {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
                    this.statusBar.setImageDrawable(new ColorDrawable(darkenColor(primaryColor)));
                else
                    this.statusBar.setImageDrawable(new ColorDrawable(primaryColor));
                this.getToolbar().setBackgroundColor(primaryColor);
            }
        }
        else {
            this.startActivity(section.getTargetIntent());
        }

        int position = section.getPosition();

        for(MaterialSection mySection : sectionList) {
            if(position != mySection.getPosition())
                mySection.unSelect();
        }
        for(MaterialSection mySection : bottomSectionList) {
            if(position != mySection.getPosition())
                mySection.unSelect();
        }

        currentSection = section;
    }

    public void setAccountListener(MaterialAccountListener listener) {
        this.accountListener = listener;
    }

    // Method used for customize layout

    public void addSection(MaterialSection section) {
        section.setPosition(sectionList.size());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(48 * density));
        sectionList.add(section);
        sections.addView(section.getView(),params);
    }

    public void addBottomSection(MaterialSection section) {
        section.setPosition(BOTTOM_SECTION_START + bottomSectionList.size());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(48 * density));
        bottomSectionList.add(section);
        bottomSections.addView(section.getView(),params);
    }

    public void addDivisor() {
        View view = new View(this);
        view.setBackgroundColor(Color.parseColor("#e0e0e0"));
        // height 1 px
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
        params.setMargins(0,(int) (8 * density), 0 , (int) (8 * density));

        sections.addView(view, params);
    }

    public void addAccount(MaterialAccount account) {
        if (accountManager.size() == 3)
            throw new RuntimeException("Currently are supported max 3 accounts");

        account.setAccountNumber(accountManager.size());
        accountManager.add(account);
    }


    /**
     * Reload Application data from Account Information
     */
    public void notifyAccountDataChanged() {
        switch(accountManager.size()) {
            case 3:
                this.setThirdAccountPhoto(findAccountNumber(MaterialAccount.THIRD_ACCOUNT).getCircularPhoto());
            case 2:
                this.setSecondAccountPhoto(findAccountNumber(MaterialAccount.SECOND_ACCOUNT).getCircularPhoto());
            case 1:
                this.setFirstAccountPhoto(currentAccount.getCircularPhoto());
                this.setDrawerBackground(currentAccount.getBackground());
                this.setUsername(currentAccount.getTitle());
                this.setUserEmail(currentAccount.getSubTitle());
            default:
        }
    }

    // create sections

    public MaterialSection newSection(String title, Drawable icon, Fragment target) {
        MaterialSection section = new MaterialSection<Fragment>(this,true,MaterialSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title, Drawable icon, Intent target) {
        MaterialSection section = new MaterialSection<Fragment>(this,true,MaterialSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title, Bitmap icon,Fragment target) {
        MaterialSection section = new MaterialSection<Fragment>(this,true,MaterialSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title, Bitmap icon,Intent target) {
        MaterialSection section = new MaterialSection<Fragment>(this,true,MaterialSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title,Fragment target) {
        MaterialSection section = new MaterialSection<Fragment>(this,false,MaterialSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title,Intent target) {
        MaterialSection section = new MaterialSection<Fragment>(this,false,MaterialSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    // abstract methods

    public abstract void init(Bundle savedInstanceState);

    // get methods

    public Toolbar getToolbar() {
        return toolbar;
    }

    public MaterialSection getCurrentSection() {
        return currentSection;
    }

    public MaterialAccount getCurrentAccount() {
        return currentAccount;
    }

    public  MaterialAccount getAccountAtCurrentPosition(int position) {

        if (position < 0 || position >= accountManager.size())
            throw  new RuntimeException("Account Index Overflow");

        return findAccountNumber(position);
    }
}