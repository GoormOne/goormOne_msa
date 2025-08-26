//package com.example.storeservice.batch;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class RunBothJobs implements CommandLineRunner {
//    private final JobLauncher jobLauncher;
//    private final Job reviewsDenormJob;
//    private final Job reviewQueryJob;
//
//    @Override
//    public void run(String... args) throws Exception {
//        jobLauncher.run(reviewsDenormJob,
//                new JobParametersBuilder().addLong("ts1", System.currentTimeMillis()).toJobParameters());
//        jobLauncher.run(reviewQueryJob,
//                new JobParametersBuilder().addLong("ts2", System.currentTimeMillis()).toJobParameters());
//    }
//}
