 function [L,U,piv] = GEPP(A)
%  Gaussian Elimination with partial pivoting 
%  for factoring n x n A 
%  only single loop, inner two loops replaced 
%  by outer product as in Van Loan
% 
% Input:  A nxn coefficient matrix
% Output: L lower triangular matrix after elimination
%         U upper triangular matrix after elimination
%         piv  vector of pointers showing row switches
%
%  +--------------------------------------------------------------------+
%  |  Copied from:                                                      |
%  |    http://www.math.udel.edu/~braun/M426/Matlab/GEPP.m (2005.02.19) |
%  |                                                                    |
%  |  Dr. Richard J. Braun                                              |
%  |  Department of Mathematical Sciences                               |
%  |  501 Ewing Hall                                                    |
%  |  University of Delaware                                            |
%  |  Newark, DE  19716                                                 |
%  |  braun@math.udel.edu                                               |
%  +--------------------------------------------------------------------+
%
[n,n] = size(A);
piv = 1:n;
% begin elimination; multipliers stored in A
for k=1:n-1
    [maxval r] = max(abs(A(k:n,k)));    % find biggest value in sub-column
    q = r+k-1;                          % compute row location in A
    piv([k q]) = piv([q k]) ;           % keep track of switching
    A([k q],:) = A([q k],:) ;           % switch row of A
    if A(k,k) ~= 0                      % if nonzero pivot, continue eliminating
        A(k+1:n,k) = A(k+1:n,k)/A(k,k);
        A(k+1:n,k+1:n) = A(k+1:n,k+1:n) - A(k+1:n,k)*A(k,k+1:n);
    end
end
U = triu(A);
L = tril(A,-1)+eye(n);
