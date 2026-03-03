package com.teamflow.config;

import com.teamflow.entity.*;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.TaskRepository;
import com.teamflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds demo data on first startup.
 *
 * Idempotent: only runs when the users table is empty.
 * This means it runs on every dev restart (create-drop schema)
 * and only once in prod (update schema — data persists between deploys).
 *
 * Excluded from test profile to avoid polluting the H2 test database.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder: database already populated, skipping.");
            return;
        }

        log.info("DataSeeder: seeding demo data...");

        // ── Users ──────────────────────────────────────────────────────────────
        userRepository.save(User.builder()
                .name("Admin")
                .email("admin@teamflow.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        User alice = userRepository.save(User.builder()
                .name("User")
                .email("alice@teamflow.com")
                .password(passwordEncoder.encode("demo123"))
                .build());

        User bob = userRepository.save(User.builder()
                .name("Member")
                .email("bob@teamflow.com")
                .password(passwordEncoder.encode("demo123"))
                .build());

        // ── Project 1: Website Redesign ────────────────────────────────────────
        Project website = projectRepository.save(Project.builder()
                .name("Website Redesign")
                .description("Modernize the public website: new design system, improved performance, mobile-first.")
                .owner(alice)
                .build());

        taskRepository.save(Task.builder()
                .title("Design new homepage layout")
                .description("Create wireframes and high-fidelity mockups in Figma for the hero section and feature blocks.")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .project(website)
                .assignee(alice)
                .build());

        taskRepository.save(Task.builder()
                .title("Set up CI/CD pipeline")
                .description("Configure GitHub Actions for automated build, test, and deploy to staging.")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.HIGH)
                .project(website)
                .assignee(bob)
                .build());

        taskRepository.save(Task.builder()
                .title("Write SEO-optimized copy")
                .description("Rewrite page titles, meta descriptions, and landing page text with target keywords.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .project(website)
                .assignee(alice)
                .build());

        taskRepository.save(Task.builder()
                .title("Optimize image assets")
                .description("Convert all images to WebP, add lazy-loading, set up CDN caching rules.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .project(website)
                .assignee(bob)
                .build());

        taskRepository.save(Task.builder()
                .title("Accessibility audit")
                .description("Run axe-core audit, fix contrast issues, add ARIA labels, ensure keyboard navigation works.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .project(website)
                .build());

        // ── Project 2: Mobile App v2 ───────────────────────────────────────────
        Project mobile = projectRepository.save(Project.builder()
                .name("Mobile App v2")
                .description("Major release: push notifications, offline mode, and redesigned task board.")
                .owner(bob)
                .build());

        taskRepository.save(Task.builder()
                .title("Implement push notifications")
                .description("Integrate Firebase Cloud Messaging for task assignments and deadline reminders.")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .project(mobile)
                .assignee(bob)
                .build());

        taskRepository.save(Task.builder()
                .title("Offline mode with local cache")
                .description("Use SQLite to cache tasks locally; sync changes when connection is restored.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .project(mobile)
                .assignee(alice)
                .build());

        taskRepository.save(Task.builder()
                .title("Fix login screen blank flash on iOS")
                .description("Root cause: white background renders before theme is applied. Fix with splash screen delay.")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.HIGH)
                .project(mobile)
                .assignee(bob)
                .build());

        taskRepository.save(Task.builder()
                .title("Write integration tests for sync logic")
                .description("Cover conflict resolution when offline edits clash with server state.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .project(mobile)
                .build());

        // ── Project 3: Q1 Marketing Campaign ──────────────────────────────────
        Project marketing = projectRepository.save(Project.builder()
                .name("Q1 Marketing Campaign")
                .description("Launch campaign across email, social media, and paid ads targeting enterprise customers.")
                .owner(alice)
                .build());

        taskRepository.save(Task.builder()
                .title("Create email drip sequence")
                .description("5-part onboarding email series: welcome, feature spotlight, case study, offer, follow-up.")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.HIGH)
                .project(marketing)
                .assignee(alice)
                .build());

        taskRepository.save(Task.builder()
                .title("Social media content calendar")
                .description("Plan 30 days of posts for LinkedIn and Twitter/X. Include product tips, testimonials, behind-the-scenes.")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .project(marketing)
                .assignee(bob)
                .build());

        taskRepository.save(Task.builder()
                .title("Set up analytics dashboard")
                .description("Track CTR, conversion rate, and CAC in Google Analytics 4. Set up funnel visualizations.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .project(marketing)
                .build());

        taskRepository.save(Task.builder()
                .title("A/B test landing page CTA")
                .description("Test 'Start free trial' vs 'Request demo' — measure 14-day conversion to paid.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .project(marketing)
                .assignee(alice)
                .build());

        log.info("DataSeeder: done. Created 3 users, 3 projects, 13 tasks.");
        log.info("  admin@teamflow.com  / admin123  (ADMIN)");
        log.info("  alice@teamflow.com  / demo123   (User)");
        log.info("  bob@teamflow.com    / demo123   (Member)");
    }
}
