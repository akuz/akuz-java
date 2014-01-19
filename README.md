akuz-java-nlp-lda
=================

LDA (Latent Dirichlet Allocation) inference in Java using 
Gibbs sampling, plus additional optimizations described below.

### Multiprocessor Parallelization 

* For faster inference on machines with multiple processors

The computations necessary for each Gibbs sampling
iteration are executed on different threads by splitting
the corpus into non-overlapping document sets, and after
each interation has completed, merging the results, which
is a negligible operation compared to sampling.

### Simulated Annealing 

* For faster convergence of Gibbs sampling

The "temperature" parameter influences the ratio of prior
Dirichlet mass to the expected additional posterior mass. For a high 
temperature of 1.0 the Dirichlet priors will be modified 
to be approximatelly of the same mass as the additional
posterior mass. This allows sampling to initially explore
wider areas of the sampling space. Then, by lowering the 
temperature in steps (after a number of iterations), we 
gradually reduce sampling space exploration variance
and converge onto the area of highest probability.
This process converges on topics much faster than
sampling with a constant (low) temperature, which
is what happens when you sample with the target
values of hyperparameters from the start.
