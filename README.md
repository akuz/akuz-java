akuz-java-lda
=============

An implementation of LDA (Latent Dirichlet Allocation) 
in Java using Gibbs sampling inference with additional
features of:

* Multiprocessor Parallelisation 
  for faster inference on machines 
  with multiple processors (cores)

The computations necessary for each Gibbs sampling
iteration are executed on different threads by splitting
the corpus into non-overlapping document sets, and after
each interation has completed, merging the results, which
is a negligible operation compared to sampling.

* Simulated Annealing
  for faster convergence of Gibbs
  sampling by following a temperature
  schedule from high to low during sampling

The "temperature" parameter influences the ratio of prior
Dirichlet mass to the expected posterior mass. For a high 
temperature of 1.0 the Dirichlet priors will be modified 
to be approximatelly of the same mass as the additional
posterior mass. This allows sampling to initially explore
wider areas of the sampling space. Then, by lowering the 
temperature in steps (after a number of iterations), we 
gradually reduce sampling space exploration variance
and converge onto the area of highest probability.
This process converges on topics much faster than
sampling with a constant (low) temperature.
