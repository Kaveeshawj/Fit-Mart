package lk.jiat.fitmart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import lk.jiat.fitmart.model.CategoryAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lk.jiat.fitmart.model.CarouselAdapter;
import lk.jiat.fitmart.model.Category;
import lk.jiat.fitmart.model.GridSpacingItemDecoration;
import lk.jiat.fitmart.model.MyOrdersFragment;
import lk.jiat.fitmart.model.Product;
import lk.jiat.fitmart.model.ProductAdapter;
import lk.jiat.fitmart.model.SuggesterProductsAdapter;
import lk.jiat.fitmart.model.updateCartBadge;
import lk.jiat.fitmart.navigations.AboutUsFragment;
import lk.jiat.fitmart.navigations.AppointmentsFragment;
import lk.jiat.fitmart.navigations.CartFragment;
import lk.jiat.fitmart.navigations.MapFragment;
import lk.jiat.fitmart.navigations.ShopFragment;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private ViewPager2 viewPager;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView headerUserNameView;
    private TextView headerEmailView;
    private ImageView headerProfileImageView;
    private FirebaseFirestore firestore;
    private List<Product> productList;
    private TextView cartBadge;
    private updateCartBadge cartBadgeManager;
    private Context context;

    private WormDotsIndicator dotsIndicator;
    private RecyclerView recyclerCategories, recyclerBestSelling;
    private NestedScrollView scrollView;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        cartBadge = findViewById(R.id.cart_badge);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", context.MODE_PRIVATE);
        String mobile = sharedPreferences.getString("mobile", "0714833745");

        cartBadgeManager = new updateCartBadge(this, cartBadge, mobile);

        ImageButton filterImageButton = findViewById(R.id.imageView7);
        filterImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AdvancedSearchActivity.class);
                startActivity(intent);
            }
        });

        TextView titleView = findViewById(R.id.textView5);
        TextView titleView1 = findViewById(R.id.textView3);
        TextView titleView2 = findViewById(R.id.text_best_selling);
        TextView titleView3 = findViewById(R.id.text_offers);
        ImageView imageView = findViewById(R.id.imageView3);
        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        recyclerCategories = findViewById(R.id.recycler_categories);
        recyclerBestSelling = findViewById(R.id.recycler_best_selling);
        scrollView = findViewById(R.id.scrollView2);
        searchView = findViewById(R.id.searchView);


        categoryrecyclerView();
        initializeViews();
        setupWindowInsets();
        setupToolbar();
        setupImageCarousel();
        setupNavigationDrawer();
        setupProfileButton();
        productrecyclerView();


        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    viewPager.setVisibility(View.VISIBLE);
                    dotsIndicator.setVisibility(View.VISIBLE);
                    recyclerCategories.setVisibility(View.VISIBLE);
                    titleView1.setVisibility(View.VISIBLE);
                    titleView2.setVisibility(View.VISIBLE);
                    titleView3.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                    productrecyclerView();
                } else {
                    viewPager.setVisibility(View.GONE);
                    dotsIndicator.setVisibility(View.GONE);
                    recyclerCategories.setVisibility(View.GONE);
                    titleView1.setVisibility(View.GONE);
                    titleView2.setVisibility(View.GONE);
                    titleView3.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                    performSearch(newText);
                }
                return true;
            }
        });




    }


    private void performSearch(String query) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("product")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> filteredProducts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("title");
                        String price = document.getString("price");
                        String imageUrl = document.getString("imageUrl");

                        // Check if the query matches any part of the title (case-insensitive)
                        if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                            Product product = new Product(name, price, imageUrl);
                            filteredProducts.add(product);
                        }
                    }
                    updateProductRecyclerView(filteredProducts);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    private void productrecyclerView() {
        productList = new ArrayList<>();
        RecyclerView suggestedRecyclerView = findViewById(R.id.recycler_best_selling);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        suggestedRecyclerView.setLayoutManager(layoutManager);

        int spanCount = 2;
        int spacing = 16;
        boolean includeEdge = true;
        suggestedRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));

        SuggesterProductsAdapter adapter = new SuggesterProductsAdapter(this, productList);
        suggestedRecyclerView.setAdapter(adapter);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("product")
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            String imageUrl1 = documentSnapshot.getString("imageUrl");
                            if (imageUrl1 == null) {
                                imageUrl1 = documentSnapshot.getString("imagePath");
                            }
                            String title1 = documentSnapshot.getString("title");
                            String price1 = documentSnapshot.getString("price");
                            Product product = new Product(title1, price1, imageUrl1);
                            productList.add(product);
                        }
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failure", e));
    }


    private void updateProductRecyclerView(List<Product> productList) {
        RecyclerView recyclerView = findViewById(R.id.recycler_best_selling);
        SuggesterProductsAdapter adapter = (SuggesterProductsAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateData(productList); // Update the adapter with the filtered data
        }
    }

    private void categoryrecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        ArrayList<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("Proteins", R.drawable.protein));
        categoryList.add(new Category("Fat burners", R.drawable.fatburner));
        categoryList.add(new Category("Creatine", R.drawable.creatine));
        categoryList.add(new Category("Protein bars", R.drawable.proteinbar));
        categoryList.add(new Category("Pre Workouts", R.drawable.preworkouts));
        categoryList.add(new Category("Vitamins", R.drawable.vitamins));

        CategoryAdapter categoryAdapter = new CategoryAdapter(this, categoryList);
        recyclerView.setAdapter(categoryAdapter);
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.viewPager);
        navigationView = findViewById(R.id.navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        View headerView = navigationView.getHeaderView(0);
        headerUserNameView = headerView.findViewById(R.id.user_name);
        headerEmailView = headerView.findViewById(R.id.user_email);
        headerProfileImageView = headerView.findViewById(R.id.profile_image);
        firestore = FirebaseFirestore.getInstance();

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.button));
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        TextView welcomeText = findViewById(R.id.nametext);
        ImageButton profileButton = findViewById(R.id.profile_button);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        String mobile = sharedPreferences.getString("mobile", null);

        if (mobile != null) {
            firestore.collection("users")
                    .whereEqualTo("mobile", mobile)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            var document = queryDocumentSnapshots.getDocuments().get(0);
                            String name = document.getString("fname");
                            String imageUrl = document.getString("profileImage");

                            welcomeText.setText("Hello " + name + "!");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(HomeActivity.this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user)
                                        .circleCrop()
                                        .into(profileButton);
                            } else {
                                profileButton.setImageResource(R.drawable.user);
                            }
                        } else {
                            welcomeText.setText("Hello Guest!");
                            profileButton.setImageResource(R.drawable.user);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user data", e);
                        welcomeText.setText("Hello Guest!");
                        profileButton.setImageResource(R.drawable.user);
                    });
        } else {
            welcomeText.setText("Hello Guest!");
            profileButton.setImageResource(R.drawable.user);
        }
    }

    private void setupImageCarousel() {
        List<Integer> imageList = Arrays.asList(
                R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image5
        );

        CarouselAdapter adapter = new CarouselAdapter(this, imageList);
        viewPager.setAdapter(adapter);

        WormDotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);
        dotsIndicator.setDotIndicatorColor(ContextCompat.getColor(this, R.color.button));
        dotsIndicator.setStrokeDotsIndicatorColor(ContextCompat.getColor(this, R.color.button));
        dotsIndicator.setViewPager2(viewPager);

        setupAutoScroll(imageList.size());
    }

    private void setupAutoScroll(int imageCount) {
        autoScrollHandler = new Handler(Looper.getMainLooper());
        autoScrollRunnable = new Runnable() {
            int currentItem = 0;

            @Override
            public void run() {
                if (currentItem == imageCount) {
                    currentItem = 0;
                }
                if (viewPager != null) {
                    viewPager.setCurrentItem(currentItem++, true);
                    autoScrollHandler.postDelayed(this, 3000);
                }
            }
        };
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            NestedScrollView scrollView = findViewById(R.id.scrollView2);
            FragmentContainerView fragmentContainer = findViewById(R.id.fragmentContainerView);
            TextView titleView = findViewById(R.id.textView5);
            SearchView searchView = findViewById(R.id.searchView);

            if (item.getItemId() == R.id.about) {
                fragmentContainer.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.INVISIBLE);
                titleView.setVisibility(View.VISIBLE);
                searchView.setVisibility(View.INVISIBLE);
                loadFragment(new AboutUsFragment());

            } else if (item.getItemId() == R.id.shop) {
                fragmentContainer.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.INVISIBLE);
                titleView.setVisibility(View.VISIBLE);
                loadShop();
            } else if (item.getItemId() == R.id.cart) {
                fragmentContainer.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.INVISIBLE);
                titleView.setVisibility(View.VISIBLE);
                loadFragment(new CartFragment());
            } else if (item.getItemId() == R.id.appointments) {
                fragmentContainer.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.INVISIBLE);
                searchView.setVisibility(View.INVISIBLE);
                titleView.setVisibility(View.VISIBLE);
                loadFragment(new AppointmentsFragment());
            } else if (item.getItemId() == R.id.help) {
                fragmentContainer.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.INVISIBLE);
                searchView.setVisibility(View.INVISIBLE);
                titleView.setVisibility(View.VISIBLE);
                loadFragment(new MapFragment());
            } else if (item.getItemId() == R.id.orders) {
                fragmentContainer.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.INVISIBLE);
                searchView.setVisibility(View.INVISIBLE);
                titleView.setVisibility(View.VISIBLE);
                loadFragment(new MyOrdersFragment());
            } else if (item.getItemId() == R.id.home) {
                fragmentContainer.setVisibility(View.INVISIBLE);
                searchView.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.VISIBLE);
            }

            if (drawerLayout != null) {
                titleView.setText(item.getTitle());
                drawerLayout.closeDrawers();
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment, null)
                .setReorderingAllowed(true)
                .commit();

    }

    private void loadShop() {
        try {
            Log.d("MainActivity", "loadShop started");
            FragmentManager fragmentManager = getSupportFragmentManager();

            // Check if fragment container exists
            View container = findViewById(R.id.fragmentContainerView);
            if (container == null) {
                Log.e("MainActivity", "Fragment container not found");
                return;
            }

            if (fragmentManager.findFragmentByTag("ShopFragment") == null) {
                try {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    ShopFragment shopFragment = new ShopFragment();
                    fragmentTransaction.replace(R.id.fragmentContainerView, shopFragment, "ShopFragment")
                            .setReorderingAllowed(true)
                            .addToBackStack(null)
                            .commit();
                    Log.d("MainActivity", "Shop fragment transaction committed");
                } catch (Exception e) {
                    Log.e("MainActivity", "Error in fragment transaction", e);
                }
            } else {
                Log.d("MainActivity", "Shop fragment already exists");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in loadShop", e);
        }
    }

    private void setupProfileButton() {
        ImageButton profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    private void updateNavigationHeader() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        String mobile = sharedPreferences.getString("mobile", null);

        if (mobile == null) {
            setDefaultProfileImage();
            return;
        }

        firestore.collection("users")
                .whereEqualTo("mobile", mobile)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        var document = queryDocumentSnapshots.getDocuments().get(0);
                        String imageUrl = document.getString("profileImage");
                        String name = document.getString("fname");
                        String email = document.getString("email");

                        if (headerUserNameView != null) headerUserNameView.setText(name);
                        if (headerEmailView != null) headerEmailView.setText(email);

                        if (imageUrl != null && !imageUrl.isEmpty() && headerProfileImageView != null) {
                            loadProfileImage(imageUrl);

                        } else {
                            setDefaultProfileImage();
                        }
                    } else {
                        setDefaultProfileImage();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data", e);
                    setDefaultProfileImage();
                });
    }

    private void loadProfileImage(String imageUrl) {
        if (headerProfileImageView != null && !isFinishing()) {
            Glide.with(this)
                    .load(imageUrl)
                    .override(500, 500)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .fallback(R.drawable.user)
                    .circleCrop()
                    .into(headerProfileImageView);
        }
    }

    private void setDefaultProfileImage() {
        if (headerProfileImageView != null) {
            headerProfileImageView.setImageResource(R.drawable.user);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
        }
        updateNavigationHeader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
            autoScrollHandler = null;
            autoScrollRunnable = null;
        }
    }
}