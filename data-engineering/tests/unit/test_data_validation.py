import pytest
import pandas as pd

def test_sample_data_loading(sample_data):
    """Test that sample data loads correctly"""
    assert 'users' in sample_data
    assert 'posts' in sample_data
    assert 'comments' in sample_data
    
    # Check users data structure (actual columns: id, full_name, email, role, created_at)
    users_df = sample_data['users']
    assert not users_df.empty
    assert 'id' in users_df.columns
    assert 'full_name' in users_df.columns
    assert 'email' in users_df.columns
    assert 'role' in users_df.columns
    
    # Check posts data structure
    posts_df = sample_data['posts']
    assert not posts_df.empty
    assert 'id' in posts_df.columns
    assert 'user_id' in posts_df.columns
    assert 'category' in posts_df.columns
    
    # Check comments data structure
    comments_df = sample_data['comments']
    assert not comments_df.empty
    assert 'id' in comments_df.columns

def test_data_quality_checks(sample_data):
    """Test basic data quality"""
    users_df = sample_data['users']
    
    # All users should have valid emails
    assert users_df['email'].str.contains('@').all()
    
    # No empty names
    assert (users_df['full_name'].str.strip() != '').all()
    
    # Valid roles
    valid_roles = ['member', 'admin']
    assert users_df['role'].isin(valid_roles).all()

def test_category_validation(sample_data):
    """Test category validation logic"""
    posts_df = sample_data['posts']
    valid_categories = ['NEWS', 'DISCUSSION', 'ALERT', 'EVENT']
    
    # All categories should be valid
    invalid_categories = ~posts_df['category'].isin(valid_categories)
    assert invalid_categories.sum() == 0