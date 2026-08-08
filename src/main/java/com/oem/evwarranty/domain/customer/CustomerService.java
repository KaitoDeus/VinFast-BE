package com.oem.evwarranty.domain.customer;


import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;
import java.util.List;
import java.util.Optional;

/**
 * Service for Customer management operations.
 */
@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findById(@NonNull Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Page<Customer> findAll(@NonNull Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Page<Customer> searchCustomers(String search, @NonNull Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return customerRepository.findAll(pageable);
        }
        return customerRepository.searchCustomers(search, pageable);
    }

    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(@NonNull Long id, Customer updatedCustomer) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setFullName(updatedCustomer.getFullName());
                    customer.setEmail(updatedCustomer.getEmail());
                    customer.setPhone(updatedCustomer.getPhone());
                    customer.setAddress(updatedCustomer.getAddress());
                    customer.setCity(updatedCustomer.getCity());
                    customer.setState(updatedCustomer.getState());
                    customer.setZipCode(updatedCustomer.getZipCode());
                    customer.setCountry(updatedCustomer.getCountry());
                    customer.setIdNumber(updatedCustomer.getIdNumber());
                    customer.setIdType(updatedCustomer.getIdType());
                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    public void deleteCustomer(@NonNull Long id) {
        customerRepository.deleteById(id);
    }

    public long count() {
        return customerRepository.count();
    }
}


