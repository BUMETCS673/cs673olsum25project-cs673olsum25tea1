// import { render, screen, waitFor } from '@testing-library/react';
// import userEvent from '@testing-library/user-event';
// import { describe, it, expect, vi, beforeEach } from 'vitest';
// import Register from '../../pages/Register'; 


// vi.mock('../../services/authService', () => ({ 
//   register: vi.fn(),
// }));
// import authService from '../../services/authService'; 

// const mockNavigate = vi.fn();
// vi.mock('react-router-dom', async (importOriginal) => {
//   const actual = await importOriginal();
//   return {
//     ...actual,
//     useNavigate: () => mockNavigate,
//   };
// });

// describe('RegisterPage Unit Tests', () => {
//     beforeEach(() => {
//         authService.register.mockClear();
//         mockNavigate.mockClear();
//     });

//     it('should allow a user to register successfully and navigate', async () => {
//         authService.register.mockResolvedValueOnce({ message: 'Registration successful', userId: '123' });
        
//         render(<RegisterPage />);
//         const user = userEvent.setup();

//         await user.type(screen.getByLabelText(/username/i), 'newuser');
//         await user.type(screen.getByLabelText(/email/i), 'newuser@example.com');
//         await user.type(screen.getByLabelText(/^password$/i), 'password');
//         await user.type(screen.getByLabelText(/confirm password/i), 'password');
        
//         await user.click(screen.getByRole('button', { name: /register/i }));

//         await waitFor(() => {
//             expect(authService.register).toHaveBeenCalledWith({
//                 username: 'newuser',
//                 email: 'newuser@example.com',
//                 password: 'password',
//             });
//         });

        
//         await waitFor(() => expect(screen.getByText(/registration successful/i)).toBeInTheDocument());

//         await waitFor(() => {
//             expect(mockNavigate).toHaveBeenCalledWith('/login');
//         });
//     });

//     it('should display an error message if registration fails (e.g., email exists)', async () => {
//         authService.register.mockRejectedValueOnce({ 
//             response: { data: { message: 'Email already exists' } }
//         });
        
//         render(<RegisterPage />);
//         const user = userEvent.setup();

//         await user.type(screen.getByLabelText(/username/i), 'testuser');
//         await user.type(screen.getByLabelText(/email/i), 'existing@example.com');
//         await user.type(screen.getByLabelText(/^password$/i), 'Password123!');
//         await user.type(screen.getByLabelText(/confirm password/i), 'Password123!');
        
//         await user.click(screen.getByRole('button', { name: /register/i }));

//         await waitFor(() => {
//             expect(screen.getByText(/email already exists/i)).toBeInTheDocument();
//         });
//         expect(mockNavigate).not.toHaveBeenCalled();
//     });

//     it('should display a client-side validation error if passwords do not match', async () => {
//         render(<RegisterPage />);
//         const user = userEvent.setup();

//         await user.type(screen.getByLabelText(/^password$/i), 'Password123!');
//         await user.type(screen.getByLabelText(/confirm password/i), 'Password différent!');
        
//         await user.click(screen.getByRole('button', { name: /register/i }));
        
//         await waitFor(() => {
//             expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
//         });
        
//         expect(authService.register).not.toHaveBeenCalled();
//         expect(mockNavigate).not.toHaveBeenCalled();
//     });

    
// });